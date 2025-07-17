package com.vt.createmanagesubmit.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.vt.createmanagesubmit.servicios.ServicioTareasProgramadas;

public class RevisionTareas implements Job{

    @Autowired
    private ServicioTareasProgramadas tareaService;

    @Override
    public void execute(JobExecutionContext context) {
        tareaService.ejecutarTareasPendientes();
    }
}
