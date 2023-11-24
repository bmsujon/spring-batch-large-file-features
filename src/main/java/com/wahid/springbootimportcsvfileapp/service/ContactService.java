package com.wahid.springbootimportcsvfileapp.service;

import com.amazonaws.services.cloudformation.model.AlreadyExistsException;
import com.amazonaws.services.personalizeevents.model.InvalidInputException;
import com.wahid.springbootimportcsvfileapp.dto.ContactAddRequestRest;
import com.wahid.springbootimportcsvfileapp.entity.Contact;
import com.wahid.springbootimportcsvfileapp.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContactService {
    @Autowired
    private ContactRepository repository;


    public Contact createContact(ContactAddRequestRest request) {
        if(request.getEmail() == null || request.getEmail().isEmpty())
            throw new InvalidInputException("email required");
        Contact contact = repository.findByEmail(request.getEmail());
        if(contact != null) {
            throw new AlreadyExistsException("Contact already exists");
        }
            contact = Contact.builder()
                    .email(request.getEmail())
                    .dateOfBirth(request.getDateOfBirth())
                    .firstName(request.getFirstName())
                    .gender(request.getGender())
                    .jobTitle(request.getJobTitle())
                    .lastName(request.getLastName())
                    .phoneNumber(request.getPhoneNumber())
                    .build();
            contact = repository.save(contact);
        return contact;
    }
}
