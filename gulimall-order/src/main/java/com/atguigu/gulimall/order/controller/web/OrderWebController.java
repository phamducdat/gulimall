package com.atguigu.gulimall.order.controller.web;

import com.atguigu.common.exception.NoStockException;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    /**
     * Submit order (place order)
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {
        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
            if (responseVo.getCode() == 0) {
                // successfully ordered
                model.addAttribute("submitOrderResp", responseVo);
                // Jump to the payment page
                return "pay";
            } else {
                // Failure to place an order-> Return to order confirmation page
                String msg = "Failure to place an order:";
                switch (responseVo.getCode()) {
                    case 1:
                        msg += "The order information expires, please submit it again after refreshing";
                        break;
                    case 2:
                        msg += "The price of the order product changes, please confirm it after submitting again";
                        break;
                    case 3:
                        msg += "The inventory lock fails, the commodity inventory is insufficient";
                        break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                // Jump to order confirmation page
                return "redirect:http://order.gulimall.com/toTrade";
            }
        } catch (Exception e) {
            // Throw out the exception-> Return to order confirmation page
            e.printStackTrace();
            if (e instanceof NoStockException) {
                String message = ((NoStockException) e).getMessage();
                redirectAttributes.addFlashAttribute("msg", message);
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }

    /**
     * Encapsulates the OrderConfirmVo object and redirects to the order confirmation page.
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        // Encapsulate OrderConfirmVo object
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("data", confirmVo);
        return "confirm";
    }

}
