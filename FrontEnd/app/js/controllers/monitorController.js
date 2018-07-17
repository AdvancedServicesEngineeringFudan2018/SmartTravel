'use strict'
angular.module('myApp.monitor')
    .controller('MonitorCtrl', function ($http, $scope, VesselProcessService, $interval, Session, $filter) {

        /**
         * Start Vessel process
         */
        $scope.vid = '413362260';  //船的id
        $scope.sailor = 'admin';    //操作员
        $scope.startVessel = function () {
            var params = {
                sailor: $scope.sailor,
                vid: $scope.vid,
                defaultDelayHour: defaultDelayHour,
                zoomInVal: zoomInVal
            };

            $http.post(vesselA_server + "api/process-instances/vessel-part", params)
                .success(function (data) {
                    console.log("Start Vessel process ...", data);
                });
        };

        /**
         * cost display
         */
        $scope.currentCost = '0.00';  //当前成本
        $scope.initialCost = '0.00'; //初始成本
        $scope.eventType = null;
        $scope.C0 = 0; //变化前，前往上一次决策的目标港口的预计总成本
        $scope.C1 = 0; //变化后，前往上一次决策的目标港口的预计总成本: C1 = C0 +　仓储费变化
        $scope.C2 = 0; //决策后，前往新的目标港口的预计总成本
        $scope.C1C0 = '';
        $scope.C2C0 = '';
        $scope.C2C1 = '';
        $scope.costRatePanelShow = false;

        AMapUI.load(['ui/misc/PathSimplifier'], function (PathSimplifier) {

            if (!PathSimplifier.supportCanvas) {
                alert('当前环境不支持 Canvas！');
                return;
            }

            $scope.displayPort = function (pname, portImg) {
                var params = {
                    params: {
                        name: pname
                    }
                }
                console.log(params);
                $http.get(vesselA_server + "api/location", params)
                    .success(function (data) {
                        console.log("load valid ports: ", data);
                        portMarkers.push(new AMap.Marker({
                            map: map,
                            icon: portImg,
                            position: new AMap.LngLat(data.longitude, data.latitude),
                            title: data.name
                        }));
                    })
            }

            /**
             * **************************************************************************************************
             * TODO : some logics associated with vessel and vessel
             * ************************************************************************************************
             */
            //Init Map
            initMap();
            var portMarkers = []; //港口点标记集合，Marker类集合

            $scope.IoTHttpUrl = IoTServer + 'sps';
            $scope.IoTSocket = new SockJS($scope.IoTHttpUrl);
            $scope.IoTStompClient = Stomp.over($scope.IoTSocket);
            $scope.vmarker = null;
            $scope.onConnected = function () {
                console.log("Connect to IoT cloud successfully.");
                $scope.IoTStompClient.subscribe("/user/queue/greetings1", $scope.onMessageReceived);

            };
            $scope.IoTStompClient.connect({}, $scope.onConnected, $scope.onError);

            $scope.onError = function (error) {
                console.log(error);
            };
            $scope.onMessageReceived = function (payload) {
                console.log("greetings form activiti :", payload);
            }

            $scope.onMeeting = function (frame) {
                $.toaster("货物交付成功！", 'success');
            }
            $scope.onMissing = function (frame) {
                $.toaster("车船已错过交付机会，货物交付失败", 'error');
            }
            $scope.onInit = function (frame) {
                var vesselShadow = JSON.parse(frame.body).vesselShadow;
                var ports = vesselShadow.destinations;
                $scope.vmarker = new AMap.Marker({ // 加点
                    map: map,
                    position: [vesselShadow.longitude, vesselShadow.latitude],
                    icon: new AMap.Icon({ // 复杂图标
                        size: new AMap.Size(64, 64), // 图标大小
                        image: "images/vessel.png",// 大图地址
                    })
                });
                //TODO:船启动后，才添加港口
                for (let i = 0; i < ports.length; i++) {
                    $scope.displayPort(ports[i].name, port32);
                }
            }
            $scope.onDockEnd = function (frame) {
                var pname = JSON.parse(frame.body).pname;
                $scope.displayPort(pname, uselessPort32);
            }
            $scope.onUpdateVesselShadow = function (frame) {
                var vesselShadow = JSON.parse(frame.body).vesselShadow;
                // console.log("Received vessel shadow : ", vesselShadow);
                var frameData = JSON.parse(frame.body);
                $scope.vesselShadows.set(vesselShadow.id, vesselShadow)
                $scope.vmarker.hide();
                $scope.vmarker = new AMap.Marker({ // 加点
                    map: map,
                    position: [vesselShadow.longitude, vesselShadow.latitude],
                    icon: new AMap.Icon({ // 复杂图标
                        size: new AMap.Size(64, 64), // 图标大小
                        image: "images/vessel.png",// 大图地址
                    })
                });
            }

        })
    })