package work.yspan.jadeplaces.yspan.work.handle.task;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import work.yspan.jadeplaces.yspan.work.util.components.WxUtil;




@Component
public class Jobs {
    private final WxUtil wxUtil;

    public Jobs(WxUtil wxUtil) {
        this.wxUtil = wxUtil;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void refreshToken() {
        wxUtil.getToken();
    }

}
