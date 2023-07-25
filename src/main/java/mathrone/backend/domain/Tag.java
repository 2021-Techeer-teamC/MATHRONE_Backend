package mathrone.backend.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "tag")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Tag{
    @Id
    Long id;
    String name;  // 수학1, 수학2 등
}
