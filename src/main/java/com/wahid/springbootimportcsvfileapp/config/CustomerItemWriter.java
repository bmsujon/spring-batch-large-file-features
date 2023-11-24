package com.wahid.springbootimportcsvfileapp.config;

import com.wahid.springbootimportcsvfileapp.entity.Contact;
import com.wahid.springbootimportcsvfileapp.repository.ContactRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerItemWriter implements ItemWriter<Contact> {

    @Autowired
    private ContactRepository repository;

    @Override
    public void write(Chunk<? extends Contact> chunk) throws Exception {
        System.out.println("Writer Thread "+Thread.currentThread().getName());
        repository.saveAll(chunk);
    }
}
