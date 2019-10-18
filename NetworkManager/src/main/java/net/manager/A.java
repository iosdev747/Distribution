//package net.manager;
//
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//class A {
//    @NonNull
//    NetManagerService netManagerService;
//
//    A() {
//        netManagerService = NetManagerService.getNetManager(this);
//    }
//
//    //return response of request
//    String onReceieve(int c) {
//        log.info("{}", c);
//        return String.valueOf(c);
//    }
//}
