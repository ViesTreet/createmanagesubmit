package com.vt.createmanagesubmit.config;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vt.createmanagesubmit.jobs.RevisionTareas;

@Configuration
public class QuartzConfig {
    @Bean
    public JobDetail revisarTareasJobDetail() {
        return JobBuilder.newJob(RevisionTareas.class)
            .withIdentity("revisionTareas")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger revisarTareasTrigger(JobDetail revisarTareasJobDetail) {
        return TriggerBuilder.newTrigger()
            .forJob(revisarTareasJobDetail)
            .withIdentity("revisionTareasTrigger")
            .withSchedule(SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInHours(1)   // cada hora
                .repeatForever()
            )
            .startNow()
            .build();
    }
}
