package com.wahid.springbootimportcsvfileapp.config;

import com.wahid.springbootimportcsvfileapp.entity.Contact;
import org.springframework.batch.item.ItemProcessor;

public class ContactProcessor implements ItemProcessor<Contact, Contact> {

    @Override
    public Contact process(Contact contact) throws Exception {
        System.out.printf(contact.toString());
        return contact;
    }
}
