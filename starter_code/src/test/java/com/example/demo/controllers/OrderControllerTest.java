package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

    private OrderController orderController;
    private final UserRepository userRepository = mock(UserRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);

    public static Item createItem(Long id) {
        Item item = new Item();
        item.setId(id);
        item.setPrice(BigDecimal.valueOf(999.99));
        item.setName("Item #" + id);
        item.setDescription("Item Description #" + id);
        return item;
    }

    public static User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("testPassword");
        return user;
    }

    @Before
    public void setUp() {
        orderController = new OrderController();
        TestUtils.injectObjects(orderController, "userRepository", userRepository);
        TestUtils.injectObjects(orderController, "orderRepository", orderRepository);

        Item item1 = createItem(1L);
        Item item2 = createItem(2L);
        User user = createUser();
        Cart cart = new Cart();
        cart.setUser(user);
        cart.addItem(item1);
        cart.addItem(item2);
        user.setCart(cart);
        UserOrder userOrder = UserOrder.createFromCart(cart);

        when(userRepository.findByUsername("test")).thenReturn(user);
        when(orderRepository.findByUser(any())).thenReturn(Collections.singletonList(userOrder));
    }

    @Test
    public void submit_success() {
        User user = new User();
        List<Item> items = new ArrayList<>();
        Cart cart = new Cart();
        cart.setItems(items);
        user.setUsername("username");
        user.setCart(cart);
        when(userRepository.findByUsername("username")).thenReturn(user);
        UserOrder savedOrder = new UserOrder();
        savedOrder.setId(1L);
        when(orderRepository.save(any())).thenReturn(savedOrder);
        final ResponseEntity<UserOrder> response = orderController.submit("username");
        assertNotNull(response);
    }

    @Test
    public void submit_user_not_found() {
        final ResponseEntity<UserOrder> response = orderController.submit("user_not_found");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void getOrdersForUser_success() {
        final ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("test");
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        List<UserOrder> userOrders = response.getBody();
        assertNotNull(userOrders);
        assertEquals(1, userOrders.size());
    }

    @Test
    public void getOrdersForUser_user_not_found() {
        when(userRepository.findByUsername("username")).thenReturn(null);
        final ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("username");
        assertEquals(404, response.getStatusCodeValue());
    }
}