package com.wahid.springbootimportcsvfileapp.controller;

import com.wahid.springbootimportcsvfileapp.dto.ContactAddRequestRest;
import com.wahid.springbootimportcsvfileapp.entity.Contact;
import com.wahid.springbootimportcsvfileapp.message.ResponseMessage;
import com.wahid.springbootimportcsvfileapp.service.ContactService;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@CrossOrigin("http://localhost:8080")
@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;

    @Autowired
    private ContactService contactService;
    private final String TEMP_STORAGE = "/Users/wahidulazam/Desktop/tmp/";

    @PostMapping
    public ResponseEntity<ResponseMessage> createContact(@RequestBody ContactAddRequestRest contactAddRequestRest) {
        Contact contact = contactService.createContact(contactAddRequestRest);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Contact created"));
    }

    @PostMapping("/upload/csv")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws IOException {

        try {
            String originalFileName = multipartFile.getOriginalFilename();
            File fileToImport = new File(TEMP_STORAGE + originalFileName);
            multipartFile.transferTo(fileToImport);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("fullPathFileName", TEMP_STORAGE + originalFileName)
                    .addLong("startAt", System.currentTimeMillis()).toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            if(execution.getExitStatus().getExitCode().equals(ExitStatus.COMPLETED)){
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Operation Successful"));
                //delete the file from the TEMP_STORAGE
//                Files.deleteIfExists(Paths.get(TEMP_STORAGE + originalFileName));
            } else if (execution.getExitStatus().getExitCode().equals(ExitStatus.FAILED)){
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage("Error occurred: " + execution.getExitStatus().getExitCode()));
            }

        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage("Error occurred: " + e.getMessage()));
        }
        return null;
    }

}
