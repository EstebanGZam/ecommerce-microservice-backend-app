package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.exception.wrapper.PaymentNotFoundException;
import com.selimhorri.app.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

        @Mock
        private PaymentRepository paymentRepository;

        @Mock
        private RestTemplate restTemplate;

        @InjectMocks
        private PaymentServiceImpl paymentService;

        private Payment testPayment;
        private PaymentDto testPaymentDto;
        private OrderDto testOrderDto;

        @BeforeEach
        void setUp() {
                testOrderDto = OrderDto.builder()
                                .orderId(1)
                                .orderDate(LocalDateTime.now())
                                .orderDesc("Test Order")
                                .orderFee(100.0)
                                .build();

                testPayment = Payment.builder()
                                .paymentId(1)
                                .orderId(1)
                                .isPayed(false)
                                .paymentStatus(PaymentStatus.IN_PROGRESS)
                                .build();

                testPaymentDto = PaymentDto.builder()
                                .paymentId(1)
                                .isPayed(false)
                                .paymentStatus(PaymentStatus.IN_PROGRESS)
                                .orderDto(OrderDto.builder().orderId(1).build())
                                .build();
        }

        @Test
        void findAll_WhenPaymentsExist_ShouldReturnPaymentDtoListWithOrderData() {
                // Given
                Payment payment1 = Payment.builder()
                                .paymentId(1)
                                .orderId(1)
                                .isPayed(false)
                                .paymentStatus(PaymentStatus.IN_PROGRESS)
                                .build();

                Payment payment2 = Payment.builder()
                                .paymentId(2)
                                .orderId(2)
                                .isPayed(true)
                                .paymentStatus(PaymentStatus.COMPLETED)
                                .build();

                List<Payment> payments = Arrays.asList(payment1, payment2);

                OrderDto order1 = OrderDto.builder()
                                .orderId(1)
                                .orderDesc("Order 1")
                                .orderFee(100.0)
                                .build();

                OrderDto order2 = OrderDto.builder()
                                .orderId(2)
                                .orderDesc("Order 2")
                                .orderFee(200.0)
                                .build();

                when(paymentRepository.findAll()).thenReturn(payments);
                when(restTemplate.getForObject(anyString(), eq(OrderDto.class)))
                                .thenReturn(order1)
                                .thenReturn(order2);

                // When
                List<PaymentDto> result = paymentService.findAll();

                // Then
                assertNotNull(result);
                assertEquals(2, result.size());

                // Verify first payment
                assertEquals(1, result.get(0).getPaymentId());
                assertEquals(false, result.get(0).getIsPayed());
                assertEquals(PaymentStatus.IN_PROGRESS, result.get(0).getPaymentStatus());
                assertEquals("Order 1", result.get(0).getOrderDto().getOrderDesc());
                assertEquals(100.0, result.get(0).getOrderDto().getOrderFee());

                // Verify second payment
                assertEquals(2, result.get(1).getPaymentId());
                assertEquals(true, result.get(1).getIsPayed());
                assertEquals(PaymentStatus.COMPLETED, result.get(1).getPaymentStatus());
                assertEquals("Order 2", result.get(1).getOrderDto().getOrderDesc());
                assertEquals(200.0, result.get(1).getOrderDto().getOrderFee());

                verify(paymentRepository, times(1)).findAll();
                verify(restTemplate, times(2)).getForObject(anyString(), eq(OrderDto.class));
        }

        @Test
        void findById_WhenPaymentExists_ShouldReturnPaymentDtoWithOrderData() {
                // Given
                Integer paymentId = 1;
                when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
                when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(testOrderDto);

                // When
                PaymentDto result = paymentService.findById(paymentId);

                // Then
                assertNotNull(result);
                assertEquals(testPayment.getPaymentId(), result.getPaymentId());
                assertEquals(testPayment.getIsPayed(), result.getIsPayed());
                assertEquals(testPayment.getPaymentStatus(), result.getPaymentStatus());
                assertEquals(testOrderDto.getOrderId(), result.getOrderDto().getOrderId());
                assertEquals(testOrderDto.getOrderDesc(), result.getOrderDto().getOrderDesc());
                assertEquals(testOrderDto.getOrderFee(), result.getOrderDto().getOrderFee());

                verify(paymentRepository, times(1)).findById(paymentId);
                verify(restTemplate, times(1)).getForObject(anyString(), eq(OrderDto.class));
        }

        @Test
        void findById_WhenPaymentNotExists_ShouldThrowPaymentNotFoundException() {
                // Given
                Integer paymentId = 999;
                when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

                // When & Then
                PaymentNotFoundException exception = assertThrows(
                                PaymentNotFoundException.class,
                                () -> paymentService.findById(paymentId));

                assertEquals("Payment with id: 999 not found", exception.getMessage());
                verify(paymentRepository, times(1)).findById(paymentId);
                verify(restTemplate, never()).getForObject(anyString(), eq(OrderDto.class));
        }

        @Test
        void save_WhenValidPaymentDto_ShouldReturnSavedPaymentDto() {
                // Given
                PaymentDto inputPaymentDto = PaymentDto.builder()
                                .isPayed(false)
                                .paymentStatus(PaymentStatus.NOT_STARTED)
                                .orderDto(OrderDto.builder().orderId(3).build())
                                .build();

                Payment savedPayment = Payment.builder()
                                .paymentId(5)
                                .orderId(3)
                                .isPayed(false)
                                .paymentStatus(PaymentStatus.NOT_STARTED)
                                .build();

                when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

                // When
                PaymentDto result = paymentService.save(inputPaymentDto);

                // Then
                assertNotNull(result);
                assertEquals(5, result.getPaymentId());
                assertEquals(false, result.getIsPayed());
                assertEquals(PaymentStatus.NOT_STARTED, result.getPaymentStatus());
                assertEquals(3, result.getOrderDto().getOrderId());

                verify(paymentRepository, times(1)).save(any(Payment.class));
        }

        @Test
        void update_WhenValidPaymentDto_ShouldReturnUpdatedPaymentDto() {
                // Given
                PaymentDto updatePaymentDto = PaymentDto.builder()
                                .paymentId(1)
                                .isPayed(true)
                                .paymentStatus(PaymentStatus.COMPLETED)
                                .orderDto(OrderDto.builder().orderId(1).build())
                                .build();

                Payment updatedPayment = Payment.builder()
                                .paymentId(1)
                                .orderId(1)
                                .isPayed(true)
                                .paymentStatus(PaymentStatus.COMPLETED)
                                .build();

                when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);

                // When
                PaymentDto result = paymentService.update(updatePaymentDto);

                // Then
                assertNotNull(result);
                assertEquals(1, result.getPaymentId());
                assertEquals(true, result.getIsPayed());
                assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
                assertEquals(1, result.getOrderDto().getOrderId());

                verify(paymentRepository, times(1)).save(any(Payment.class));
        }

        @Test
        void deleteById_WhenValidPaymentId_ShouldCallRepositoryDelete() {
                // Given
                Integer paymentId = 1;
                doNothing().when(paymentRepository).deleteById(paymentId);

                // When
                assertDoesNotThrow(() -> paymentService.deleteById(paymentId));

                // Then
                verify(paymentRepository, times(1)).deleteById(paymentId);
        }

        @Test
        void findAll_WhenPaymentStatusIsCompleted_ShouldReturnCompletedPayments() {
                // Given
                Payment completedPayment = Payment.builder()
                                .paymentId(1)
                                .orderId(1)
                                .isPayed(true)
                                .paymentStatus(PaymentStatus.COMPLETED)
                                .build();

                List<Payment> payments = Arrays.asList(completedPayment);
                OrderDto orderDto = OrderDto.builder()
                                .orderId(1)
                                .orderDesc("Completed Order")
                                .orderFee(150.0)
                                .build();

                when(paymentRepository.findAll()).thenReturn(payments);
                when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(orderDto);

                // When
                List<PaymentDto> result = paymentService.findAll();

                // Then
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(PaymentStatus.COMPLETED, result.get(0).getPaymentStatus());
                assertEquals(true, result.get(0).getIsPayed());
                assertEquals("Completed Order", result.get(0).getOrderDto().getOrderDesc());

                verify(paymentRepository, times(1)).findAll();
                verify(restTemplate, times(1)).getForObject(anyString(), eq(OrderDto.class));
        }

        @Test
        void save_WhenPaymentStatusIsInProgress_ShouldMaintainCorrectStatus() {
                // Given
                PaymentDto inputPaymentDto = PaymentDto.builder()
                                .isPayed(false)
                                .paymentStatus(PaymentStatus.IN_PROGRESS)
                                .orderDto(OrderDto.builder().orderId(2).build())
                                .build();

                Payment savedPayment = Payment.builder()
                                .paymentId(3)
                                .orderId(2)
                                .isPayed(false)
                                .paymentStatus(PaymentStatus.IN_PROGRESS)
                                .build();

                when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

                // When
                PaymentDto result = paymentService.save(inputPaymentDto);

                // Then
                assertNotNull(result);
                assertEquals(PaymentStatus.IN_PROGRESS, result.getPaymentStatus());
                assertEquals(false, result.getIsPayed());
                assertEquals(2, result.getOrderDto().getOrderId());

                verify(paymentRepository, times(1)).save(any(Payment.class));
        }
}