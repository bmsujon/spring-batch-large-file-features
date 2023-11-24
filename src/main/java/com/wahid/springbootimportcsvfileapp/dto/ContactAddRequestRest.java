package com.wahid.springbootimportcsvfileapp.dto;

import lombok.*;


@Data
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContactAddRequestRest {
    private String firstName;

    private String lastName;

    private String gender;

    private String phoneNumber;

    private String email;

    private String dateOfBirth;

    private String jobTitle;
}
