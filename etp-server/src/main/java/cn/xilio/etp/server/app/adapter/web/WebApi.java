package cn.xilio.etp.server.app.adapter.web;

import cn.xilio.etp.server.app.Api;
import cn.xilio.etp.server.app.adapter.dto.res.ClientListDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api")
@Controller
public class WebApi implements Api {
    @Override
    public ClientListDTO list() {
        return null;
    }
}
