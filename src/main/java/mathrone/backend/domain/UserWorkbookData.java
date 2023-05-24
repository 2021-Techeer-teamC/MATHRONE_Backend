package mathrone.backend.domain;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class UserWorkbookData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String workbookId;

    @NotNull
    private String title;

    @NotNull
    private String img;

    @NotNull
    private String publisher;

    private String level;
    private Boolean star;

}
