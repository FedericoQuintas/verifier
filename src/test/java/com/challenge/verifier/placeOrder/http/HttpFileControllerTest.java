package com.challenge.verifier.placeOrder.http;

import com.challenge.verifier.placeOrder.domain.*;
import com.challenge.verifier.placeOrder.handler.PlaceOrderCommandHandler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HttpFileControllerTest {

    public static final String UPLOAD = "/upload";
    public static final int ORDERS_IN_TEST_FILE = 6;
    public static final Instant NOW = Instant.now();

    @InjectMocks
    HttpFileControllerTest httpFileController;

    @MockBean
    PlaceOrderCommandHandler placeOrderCommandHandler;

    @MockBean
    TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    @SneakyThrows
    public void uploadsImage() {

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        when(timeProvider.now()).thenReturn(NOW);

        MockMultipartFile file = new MockMultipartFile("file", new FileInputStream("test1.txt"));

        MockMvc mockMvc
                = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(multipart(UPLOAD).file(file))
                .andExpect(status().isOk());

        verify(placeOrderCommandHandler, times(ORDERS_IN_TEST_FILE)).place(captor.capture());
        assertEquals(ORDERS_IN_TEST_FILE, captor.getAllValues().size());
        Order firstOrder = captor.getAllValues().get(0);
        assertEquals(Side.BUY, firstOrder.side());
        assertEquals(Id.of(10000L), firstOrder.id());
        assertEquals(Price.of(BigDecimal.valueOf(98)), firstOrder.price());
        assertEquals(Quantity.of(25500), firstOrder.quantity());
        assertEquals(NOW, firstOrder.timestamp());
    }

    @SneakyThrows
    @Test
    public void retrievesErrorWhenParamNameIsIncorrect() {

        MockMultipartFile file = new MockMultipartFile("file1", new FileInputStream("test1.txt"));

        MockMvc mockMvc
                = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(multipart(UPLOAD).file(file))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    public void returnsErrorMessage() {

        doThrow(new RuntimeException()).when(placeOrderCommandHandler).place(any());

        MockMultipartFile file = new MockMultipartFile("file", new FileInputStream("test1.txt"));

        MockMvc mockMvc
                = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(multipart(UPLOAD).file(file))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertEquals("Unknown error: Please try again later",
                        result.getResponse().getContentAsString()));
    }
}
