package il.cshaifasweng.OCSFMediatorExample.entities.domain;

import il.cshaifasweng.OCSFMediatorExample.entities.converters.GenderConverter;
import jakarta.persistence.*;
import java.io.Serializable;

// matched the Person class to hibernate

@MappedSuperclass
public abstract class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String phone;

    @Convert(converter = GenderConverter.class)
    @Column(nullable = false, length = 16)
    private Gender gender;

    public Person() {}

    public Person(String name, String email, String phone, Gender gender) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
}
