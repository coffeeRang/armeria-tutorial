package org.example.server.blog;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.annotation.Blocking;
import com.linecorp.armeria.server.annotation.Delete;
import com.linecorp.armeria.server.annotation.ExceptionHandler;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.Put;
import com.linecorp.armeria.server.annotation.RequestConverter;
import com.linecorp.armeria.server.annotation.RequestObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BlogService {
    private final Map<Integer, BlogPost> blogPosts = new ConcurrentHashMap<>();

    @Post("/blogs")
    @RequestConverter(BlogPostRequestConverter.class)
    public HttpResponse createBlogPost(BlogPost blogPost) {
        blogPosts.put(blogPost.getId(), blogPost);
        return HttpResponse.ofJson(blogPost);
    }

    @Get("/blogs/:id")
    public HttpResponse getBlogPost(@Param("id") int id) {
        BlogPost blogPost = blogPosts.get(id);
        return HttpResponse.ofJson(blogPost);
    }

    @Put("/blog")
    public HttpResponse updateBlogPost(@Param("id") int id, @RequestObject BlogPost blogPost) {
        // Update a blog post
        BlogPost oldBlogPost = blogPosts.get(id);
        if (oldBlogPost == null) {
            // return a Not Found error. See the next section for instructions
            return HttpResponse.of(HttpStatus.NOT_FOUND);
        }
        BlogPost newBlogPost = new BlogPost(id, blogPost.getTitle(),
                blogPost.getContent(),
                oldBlogPost.getCreatedAt(),
                blogPost.getCreatedAt()
        );
        blogPosts.put(id, newBlogPost);
        return HttpResponse.ofJson(newBlogPost);
    }

    @Blocking
    @Delete("/blogs/:id")
    @ExceptionHandler(BadRequestExceptionHandler.class)
    public HttpResponse deleteBlogPost(@Param int id) {
        BlogPost removed = blogPosts.remove(id);

        if (removed == null) {
            throw new IllegalArgumentException("The blog post does not exist. id: " + id);
        }
        return HttpResponse.of(HttpStatus.NO_CONTENT);
    }

}
