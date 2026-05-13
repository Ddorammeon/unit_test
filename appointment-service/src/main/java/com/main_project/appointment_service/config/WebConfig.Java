import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ URL /images/** tới thư mục uploads/images/ trên hệ thống file
        registry.addResourceHandler("/images/**")
                //hệ thống gửi file ảnh laị cho request
                .addResourceLocations("file:uploads/images/");
    }
}