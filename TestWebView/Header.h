//
//  Header.h
//  
//
//  Created by ramonqlee on 3/20/15.
//
//

#ifndef _Header_h
#define _Header_h

//新加场景
'Secne'  =>  array(
                   'cancel_4_service1'                 => '客服取消订单-未付款',
                   'cancel_4_service2'                 => '客服取消订单-已付定金、预约先付',
                   'cancel_4_service3'                 => '客服取消订单-已付清&未签到、已签到&未上车',
                   'cancel_4_service4'                 => '客服取消订单-异常',
                   'news_reservation_succ_car_all_pay' => '预定成功，支付全款',
                   'news_coupon3'                      => '优惠券通知',
                   'sms_feeyo_booking_ok'              => '下单成功后推送',
                   'sms_feeyo_flight_arr'              => '飞机落地时推送',
                   'sms_feeyo_driver_reach'            => '司机点击到达时推送',
                   'sms_feeyo_booking_cancel'          => '预订成功页面取消订单',
                   'sms_feeyo_timeout_cancel'          => '超过登车时间被候车小姐取消',
                   'sms_feeyo_card_cancel'             => '登车牌页面取消订单',
                   'sms_channel_new_order_service_add' => '后台客服下单',
                   'qr_msg_AVSDF'                      => '二维码（弃用）',
                   'sms_channel_new_order_add'         => '下单',
                   'veryzhun_coupon_fy'                => '飞常准优惠券',
                   'sms_channel_new_order_jss'         => '金色世纪',
                   'order_again_sent_car'              => '订单被重新派车',

                   //V1.2 送机（乘客消息）
                   'order_created_by_service'          => '客服下单完毕',
                   'order_paid_by_client'              => '乘客支付完毕',
                   'order_confirmed_by_client'         => '乘客已经二次确认',
                   'car_alloc_success'                 => '派车成功',
                   'driver_arrived'                    => '司机到达',
                   'car_4_you_go'                      => '乘客已出发',
                   'car_4_you_arrived'                 => '乘客到达',
                   'your_bill'                         => '账单',
                   'order_canceled_by_client'          => '乘客在付款后，可以取消的时候取消订单',
                   'order_will_be_canceled_alarm'      => '即将取消提醒',
                   'unpaid_order_canceled'             => '未付款自动取消',
                   
                   // V1.2 运营消息
                   'order_to_be_confirmed'             => '提醒客服二次确认',
                   'order_to_be_confirmed_sos'         => '超时未确认',
                   'order_forecast'                    => '订单预告，逻辑同现在接机',
                   'order_with_super_service_forecast' => '订单超值预告，发送逻辑同现在接机',
                   'car_auto_alloc_1'                  => '系统自动派车成功（调度/客服）',
                   'car_auto_alloc_2'                  => '系统自动派车成功（司机）',
                   'car_to_be_manual_alloc'            => '系统自动派车失败&停车场有车',
                   'no_car'                            => '系统自动派车失败&停车场无车',
                   'car_will_be_late_1'                => '提前20分钟未到上车地（调度/客服）',
                   'car_will_be_late_2'                => '提前20分钟未到上车地（司机）',
                   'super_service_client_start_1'      => '超值乘客出发（客服/运营/白云）',
                   'super_service_client_start_2'      => '超值乘客出发（司机）',
                   'super_service_client_will_arrive'  => '超值乘客快到机场',
                   'order_canceled_after_car_alloc_1'  => '已派车订单取消（调度）',
                   'order_canceled_after_car_alloc_2'  => '已派车订单取消（司机）',
                   ),

#endif
