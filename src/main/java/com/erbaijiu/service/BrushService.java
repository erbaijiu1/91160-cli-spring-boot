package com.erbaijiu.service;

import com.erbaijiu.entity.Config;
import com.erbaijiu.entity.ScheduleInfo;
import com.erbaijiu.enums.BrushChannelEnum;

import java.util.List;

/**
 * @author pengpan
 */
public interface BrushService {

    TicketService getTicketService(BrushChannelEnum brushChannel);

    List<ScheduleInfo> getTicket(Config config);
}
