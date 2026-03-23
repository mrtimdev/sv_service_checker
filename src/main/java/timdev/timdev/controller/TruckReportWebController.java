package timdev.timdev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/scales/trucks/report")
public class TruckReportWebController {

    @GetMapping("/form")
    public String getReportForm(Model model) {

        return "/scale-reports/index";
    }

}
