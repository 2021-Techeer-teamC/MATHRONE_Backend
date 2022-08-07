package mathrone.backend.domain;

import com.sun.istack.NotNull;
import lombok.NoArgsConstructor;


import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@Entity
public class bookContent {

    @Id
    private Long id;

    @NotNull
    private String publisher;

    @NotNull
    @ElementCollection // make "List: available -> not from DB : no @type
    private List<String> categories;


    //생성자
    public bookContent(Long id, String publisher, List<String> categories){
        this.id=id;
        this.publisher=publisher;
        this.categories=categories;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
