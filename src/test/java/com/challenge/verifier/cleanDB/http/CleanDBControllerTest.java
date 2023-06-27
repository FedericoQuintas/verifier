package com.challenge.verifier.cleanDB.http;

import com.challenge.verifier.matchOrder.ports.OrdersPriorityQueue;
import com.challenge.verifier.matchOrder.ports.TradesLogWriter;
import com.challenge.verifier.placeOrder.ports.OrderPlacedPublisher;
import com.challenge.verifier.queryOrder.ports.OrderEventRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CleanDBControllerTest {

    public static final String CLEAN = "/clean";

    @MockBean
    OrderPlacedPublisher orderPlacedPublisher;

    @MockBean
    OrderEventRepository orderEventRepository;

    @MockBean
    TradesLogWriter tradesLogWriter;

    @MockBean
    OrdersPriorityQueue ordersPriorityQueue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    @SneakyThrows
    public void cleansDB() {

        MockMvc mockMvc
                = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(post(CLEAN))
                .andExpect(status().isOk());

        verify(orderEventRepository).deleteAll();
        verify(ordersPriorityQueue).deleteAll();
        verify(tradesLogWriter).deleteAll();
    }

    @SneakyThrows
    @Test
    public void returnsErrorMessage() {

        doThrow(new RuntimeException()).when(orderEventRepository).deleteAll();

        MockMvc mockMvc
                = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(post(CLEAN))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertEquals("Please try again later",
                        result.getResponse().getContentAsString()));
    }
}
