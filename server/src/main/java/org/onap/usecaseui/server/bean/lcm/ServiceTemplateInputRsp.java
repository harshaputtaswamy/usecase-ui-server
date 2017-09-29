/**
 * Copyright 2016-2017 ZTE Corporation.
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
package org.onap.usecaseui.server.bean.lcm;

import com.google.common.base.MoreObjects;
import org.onap.usecaseui.server.service.lcm.domain.aai.bean.VimInfo;

import java.util.List;
import java.util.Objects;

public class ServiceTemplateInputRsp {

    private List<ServiceTemplateInput> serviceTemplateInput;

    private List<VimInfo> vimInfos;

    public ServiceTemplateInputRsp(List<ServiceTemplateInput> serviceTemplateInput, List<VimInfo> vimInfos) {
        this.serviceTemplateInput = serviceTemplateInput;
        this.vimInfos = vimInfos;
    }

    public List<ServiceTemplateInput> getServiceTemplateInput() {
        return serviceTemplateInput;
    }

    public List<VimInfo> getVimInfos() {
        return vimInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceTemplateInputRsp that = (ServiceTemplateInputRsp) o;
        return Objects.equals(serviceTemplateInput, that.serviceTemplateInput) &&
                Objects.equals(vimInfos, that.vimInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceTemplateInput, vimInfos);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("serviceTemplateInput", serviceTemplateInput)
                .add("vimInfos", vimInfos)
                .toString();
    }
}