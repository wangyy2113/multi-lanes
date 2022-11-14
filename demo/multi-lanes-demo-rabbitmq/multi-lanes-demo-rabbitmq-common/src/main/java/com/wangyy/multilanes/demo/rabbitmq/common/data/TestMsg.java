package com.wangyy.multilanes.demo.rabbitmq.common.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestMsg implements Serializable {

    private String msg;

}
