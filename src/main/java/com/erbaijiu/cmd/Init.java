package com.erbaijiu.cmd;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.erbaijiu.common.store.AccountStore;
import com.erbaijiu.common.store.ConfigStore;
import com.erbaijiu.entity.InitData;
import com.erbaijiu.entity.Prop;
import com.erbaijiu.enums.InitDataEnum;
import com.erbaijiu.service.CoreService;
import com.erbaijiu.service.LoginService;
import com.erbaijiu.util.CommonUtil;
import io.airlift.airline.Command;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;
//import org.jsoup.internal.StringUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pengpan
 */
@Slf4j
@Command(name = "init", description = "Initialization data")
public class Init implements Runnable {

    private final Scanner in = new Scanner(System.in);

    private final CoreService coreService = SpringUtil.getBean(CoreService.class);
    private final LoginService loginService = SpringUtil.getBean(LoginService.class);
    private String userName;
    private String passWd;
    private String patient_id = null;

    public void setUserAndPass(String userName, String passWd){
        this.userName = userName;
        this.passWd = passWd;
    }

    public void setPatient_id(String patient_id){
        this.patient_id= patient_id;
    }

    @Override
    public void run() {
        login();
        initData(InitDataEnum.MEMBER);
        initData(InitDataEnum.CITY);
        initData(InitDataEnum.UNIT);
        initData(InitDataEnum.DEPT);
        initData(InitDataEnum.DOCTOR);
        initData(InitDataEnum.WEEK);
        initData(InitDataEnum.DAY);
        storeConfig();
        CommonUtil.normalExit("init success.");
    }

    private void login() {
        boolean loginSuccess;
        do {
            if(StringUtils.isBlank(this.userName)){
                userName = AccountStore.getUserName();
                while (StrUtil.isBlank(userName)) {
                    System.out.print("请输入用户名: ");
                    userName = in.nextLine();
                }

                passWd = AccountStore.getPassword();
                while (StrUtil.isBlank(passWd)) {
                    System.out.print("请输入密码: ");
                    passWd = in.nextLine();
                }
            }
            log.info("登录中，请稍等...");

            loginSuccess = loginService.doLogin(userName, passWd);

        } while (!loginSuccess);
    }

    private void initData(InitDataEnum initDataEnum) {
        log.info("");
        InitData initData = initDataEnum.getInitData();
        log.info(initData.getBanner());
        List<String> ids = new ArrayList<>();
        List<Map<String, Object>> data = initData.getData().apply(coreService);
        Map<String, String> idNamePairs = new HashMap<>();
        int i = 0;
        for (Map<String, Object> datum : data) {
            String id = String.valueOf(datum.get(initData.getAttrId()));
            ids.add(id);
            String name = StrUtil.format("[{}-{}]. {}", i, id, datum.get(initData.getAttrName()));
            idNamePairs.put(id, datum.get(initData.getAttrName()).toString());
            log.info(name);
            i++;
        }

        List<String> backList = new ArrayList<>();
        backList.addAll(ids);
        boolean success;
        String matchZH = ".*[\\u4E00-\\u9FA5]+.*";
        do {
            String id = null;
            while (StrUtil.isBlank(id)) {
                ids.clear();
                ids.addAll(backList);

                System.out.println(initData.getInputTips() + "(输入中文将在结果中查询：)");
                String userIn = in.nextLine();

                if(null != userIn && userIn.trim().matches(matchZH)){
                    String finalUserIn = userIn;
                    Map<String, String> targetMap = idNamePairs.entrySet().stream().filter(p->p.getValue().contains(finalUserIn)).
                            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    ids.clear();
                    log.info("查询结果如下，请再次选择：");
                    targetMap.forEach((key, value) -> {
                        ids.add(key);
                        String outPutStr = String.format("[%d-%s:%s]", ids.size() - 1, key, value);
                        System.out.println(outPutStr);
                    });
                    System.out.println();
                    System.out.println("请重新输入编号：");

                    id = in.nextLine();
                }
                else{
                    id = userIn;
                }
            }
            if(id.matches(matchZH) || id.contains("，") || ids.isEmpty()){
                success = false;
                log.error("input error.id:{}, ids:{}", id, ids);
                continue;
            }
//            id = ids.get(Integer.valueOf(id));
            id = getRealIds(ids, id);
            success = checkInput(ids, id);
            if (success) {
                initData.getStore().accept(id);
            } else {
                log.warn("输入有误，请重新输入！");
            }
        } while (!success);
    }

    private String getRealIds(List<String> ids, String id){
        List<String> split = StrUtil.split(id, ',');
        return split.stream()
                .map(s->ids.get(Integer.parseInt(s))).collect(Collectors.joining(","));
    }

    private boolean checkInput(List<String> ids, String id) {
        if (CollUtil.isEmpty(ids) || StrUtil.isBlank(id)) {
            return false;
        }
        if (ids.contains(id)) {
            return true;
        }
        List<String> split = StrUtil.split(id, ',');
        for (String s : split) {
            if (!ids.contains(s)) {
                return false;
            }
        }
        return true;
    }

    private void storeConfig() {
        List<Prop> props = new ArrayList<>();
        props.add(new Prop("91160账号", "userName", AccountStore.getUserName()));
        props.add(new Prop("91160密码", "password", AccountStore.getPassword()));
        props.add(new Prop("就诊人编号", "memberId", ConfigStore.getMemberId()));
        props.add(new Prop("城市编号", "cityId", ConfigStore.getCityId()));
        props.add(new Prop("医院编号", "unitId", ConfigStore.getUnitId()));
        props.add(new Prop("科室编号", "deptId", ConfigStore.getDeptId()));
        props.add(new Prop("医生编号", "doctorId", ConfigStore.getDoctorId()));
        props.add(new Prop("需要周几的号[可多选，如(6,7)]", "weeks", ConfigStore.getWeekId()));
        props.add(new Prop("时间段编号[可多选，如(am,pm)]", "days", ConfigStore.getDayId()));
        props.add(new Prop("刷号休眠时间[单位:毫秒]", "sleepTime", "5000"));
        props.add(new Prop("刷号起始日期(表示刷该日期后一周的号,为空取当前日期)[格式: 2022-06-01]", "brushStartDate", ""));
        // custom config
        props.add(new Prop("是否开启定时挂号[true/false]", "enableAppoint", "false"));
        props.add(new Prop("定时挂号时间[格式: 2022-06-01 15:00:00]", "appointTime", ""));
        props.add(new Prop("是否开启代理[true/false]", "enableProxy", "false"));
        props.add(new Prop("代理文件路径[格式: /dir/proxy.txt]", "proxyFilePath", "proxy.txt"));
        props.add(new Prop("获取代理方式[ROUND_ROBIN(轮询)/RANDOM(随机)]", "proxyMode", "ROUND_ROBIN"));
        props.add(new Prop("刷号通道[CHANNEL_1(通道1)/CHANNEL_2(通道2)]", "brushChannel", ""));

        StringBuilder sb = new StringBuilder();
        for (Prop prop : props) {
            String line1 = String.format("# %s", prop.getNote());
            String line2 = String.format("%s=%s", prop.getKey(), prop.getValue());
            sb.append(line1).append(System.lineSeparator()).append(line2).append(System.lineSeparator());
        }

        File file = new File("config.properties");
        FileUtil.writeUtf8String(sb.toString(), file);
        log.info("The file config.properties has been generated.");
    }

}
