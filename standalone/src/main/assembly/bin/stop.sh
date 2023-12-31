#!/bin/sh
#
# Copyright 2018 CMCC Corporation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

function shutdown_usecaseui_server(){
    echo ================== usecase-ui server shutdown is starting =============================================
    curl -X POST http://127.0.0.1:8082/api/usecaseui-server/v1/shutdown
    echo ================== usecase-ui server shutdown finished =============================================
}

function shutdown_usecaseui_db(){
    echo ================== usecase-ui database shutdown is starting =============================================
    service postgresql stop
    echo ================== usecase-ui database shutdown finished =============================================
}

shutdown_usecaseui_server;
shutdown_usecaseui_db;
echo "*****************usecase server shutdown finished*****************"
