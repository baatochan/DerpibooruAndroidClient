package derpibooru.derpy.server.parsers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import derpibooru.derpy.data.server.DerpibooruComment;
import derpibooru.derpy.data.server.DerpibooruTagDetailed;
import derpibooru.derpy.server.parsers.objects.ImageFilterParserObject;
import derpibooru.derpy.ui.views.htmltextview.ImageActionLink;

public class CommentParser implements ServerResponseParser<DerpibooruComment> {
    private static final Pattern PATTERN_COMMENT_ID = Pattern.compile("^(?:comment_)([\\d]*)");
    private static final String IMAGE_CONTAINER_SELECTOR = "div.image-show-container";

    private final ImageFilterParserObject mFilter;

    public CommentParser(List<DerpibooruTagDetailed> spoileredTags, List<Integer> hiddenTagIds) {
        mFilter = new ImageFilterParserObject(spoileredTags, hiddenTagIds);
    }

    @Override
    public DerpibooruComment parseResponse(String rawResponse) throws Exception {
        Document doc = Jsoup.parse(rawResponse);

        Element comment = doc.select("div.post-content").first();
        Element commentOptions = doc.select("div.post-options").first();

        int id = parseId(doc.select("article").first());
        String author = parseAuthor(comment);
        String avatarUrl = parseAvatarUrl(comment);
        String text = parseCommentBody(comment);
        String postedAt = parsePostedAt(commentOptions);
        return new DerpibooruComment(id, author, avatarUrl, text, postedAt);
    }

    private int parseId(Element article) {
        Matcher m = PATTERN_COMMENT_ID.matcher(article.attr("id"));
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private String parseAuthor(Element commentContent) {
        /* TODO: parse comment author's badges */
        return commentContent.select(".post-author").first().text().trim();
    }

    private String parseAvatarUrl(Element commentContent) {
        return "https:" + commentContent.select("img").first().attr("src");
    }

    private String parseCommentBody(Element commentContent) throws JSONException {
        Element post = commentContent.select("div.post-text").first();
        post.select("span.spoiler").tagName("spoiler").removeAttr("class");
        processCommentImages(post);
        return post.html();
    }

    private String parsePostedAt(Element commentOptions) {
        Element time = commentOptions.select("div").first().select("time").first();
        return time.attr("datetime");
    }

    private void processCommentImages(Element postBody) throws JSONException {
        processExternalImages(postBody);
        processEmbeddedImages(postBody);
    }

    private void processEmbeddedImages(Element postBody) throws JSONException {
        while (!postBody.select(IMAGE_CONTAINER_SELECTOR).isEmpty()) {
            Element imageContainer = postBody.select(IMAGE_CONTAINER_SELECTOR).get(0);
            JSONArray imageTags = new JSONArray(imageContainer.attr("data-image-tags"));

            String mainImage = getImageInContainer(imageContainer, "div.image-show");
            String filterImage = mFilter.getImageHiddenUrl(imageTags);
            if (filterImage.isEmpty()) {
                filterImage = mFilter.getImageSpoilerUrl(imageTags);
                if (filterImage.isEmpty()) {
                    filterImage = null;
                }
            }

            postBody.select(IMAGE_CONTAINER_SELECTOR).get(0).replaceWith(
                    getEmbeddedImageElement(filterImage, mainImage));
        }
    }

    private void processExternalImages(Element postBody) {
        /* select all GIF images that are not embedded (do not have "data-image-id" attribute) */
        for (Element image : postBody.select("img:not([data-image-id])[src~=([^/]*(gif.*))$]")) {
            ImageActionLink.LinkInserter.wrapGifImage(image);
        }
    }

    private Element getEmbeddedImageElement(@Nullable String filterImage, @NonNull String mainImage) {
        if (filterImage != null) {
            return ImageActionLink.LinkInserter.getWrappedEmbeddedImage(filterImage, mainImage);
        } else {
            return ImageActionLink.LinkInserter.getWrappedEmbeddedImage(mainImage);
        }
    }

    @NonNull
    private String getImageInContainer(Element imageContainer, String elementSelector) {
        Element hiddenContainer = imageContainer.select(elementSelector).first();
        return "https:" + hiddenContainer.select("img").attr("src");
    }
}
