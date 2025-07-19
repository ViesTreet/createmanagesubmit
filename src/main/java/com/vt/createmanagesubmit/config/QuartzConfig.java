package com.vt.createmanagesubmit.config;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import com.vt.createmanagesubmit.jobs.RevisionTareas;

@Configuration
public class QuartzConfig {

    @Autowired
    private ApplicationContext applicationContext;
    
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

     // 2) Le decimos a Quartz que use Spring para instanciar los Jobs
    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    // 3) Registramos un SchedulerFactoryBean usando ese JobFactory
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(SpringBeanJobFactory jobFactory,
                                                     Trigger revisarTareasTrigger,
                                                     JobDetail revisarTareasJobDetail) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(jobFactory);
        factory.setJobDetails(revisarTareasJobDetail);
        factory.setTriggers(revisarTareasTrigger);
        return factory;
    }
}
