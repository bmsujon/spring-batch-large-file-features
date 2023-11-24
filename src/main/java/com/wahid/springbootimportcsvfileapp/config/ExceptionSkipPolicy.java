package com.wahid.springbootimportcsvfileapp.config;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

//@Slf4j
public class ExceptionSkipPolicy implements SkipPolicy {


    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        System.out.printf(t.getMessage());
        return t instanceof Exception && skipCount < 1000;
    }
}
