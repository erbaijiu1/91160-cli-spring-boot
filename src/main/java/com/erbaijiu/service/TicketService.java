package com.erbaijiu.service;

import com.erbaijiu.entity.Config;
import com.erbaijiu.entity.ScheduleInfo;

import java.util.List;

/**
 * @author pengpan
 */
public interface TicketService {

    List<String> getKeyList(Config config);

    List<ScheduleInfo> getTicket(Config config, List<String> keyList);
}
