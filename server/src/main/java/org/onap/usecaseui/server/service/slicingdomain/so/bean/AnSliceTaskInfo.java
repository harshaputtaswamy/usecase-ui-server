/*
 * Copyright (C) 2021 CMCC, Inc. and others. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.usecaseui.server.service.slicingdomain.so.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnSliceTaskInfo {
    private String suggestNssiId;
    private String suggestNssiName;
    private String progress;
    private String status;
    private String statusDescription;
    private SliceProfile sliceProfile ;
    private String scriptName ;
    private Boolean enableNSSISelection ;
    @JsonProperty("ip_adrress")
    private String ipAdrress ;
    @JsonProperty("interface_id")
    private String interfaceId ;
    @JsonProperty("nextHop_info")
    private String nextHopInfo ;
    private String sliceInstanceId;
    private String vendor;
    private String networkType;
    private String subnetType;
    private String endPointId;
    private NstInfo nsstinfo;

}
