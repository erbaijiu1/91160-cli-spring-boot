package com.erbaijiu.cmd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CmdFactory {
    private final Register register;

    @Autowired
    public CmdFactory(Register register) {
        this.register = register;
    }

    public void doCmd(String runType){
        if(runType.equals("init")){
            Init init = new Init();
            init.run();
        }else if(runType.equals("register")){
            register.run();
        }
        else {
            log.error("not found run_type", runType);
        }
    }

}
