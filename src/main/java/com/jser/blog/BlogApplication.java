package com.jser.blog;

import com.jser.blog.utils.AjaxResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SpringBootApplication
@Controller
public class BlogApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogApplication.class, args);
	}

	@RequestMapping("/index")
	public void index(Model model) {
		//往模板上注入变量
		model.addAttribute("name", "wgd");
		model.addAttribute("age", 18);

	}

}
