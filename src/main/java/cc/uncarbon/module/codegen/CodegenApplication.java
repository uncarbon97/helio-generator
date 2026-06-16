package cc.uncarbon.module.codegen;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cc.uncarbon.module.codegen.dao")
public class CodegenApplication {

	static void main(String[] args) {
		SpringApplication.run(CodegenApplication.class, args);
	}
}
