/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.integration.efcSimple;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.integration.cfcSimple.IntegrationController;
import com.qcadoo.mes.integration.cfcSimple.IntegrationPerformer;
import com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants;

@Controller
public class IntegrationEnovaController extends IntegrationController {

    @Autowired
    @Qualifier("enovaIntegrationPerformer")
    private IntegrationPerformer integrationPerformer;

    @RequestMapping(value = EfcSimpleConstants.PLUGIN_IDENTIFIER + "/uploadPage", method = RequestMethod.GET)
    public final ModelAndView getSubiektUploadPageView(final Locale locale) {
        return getUploadPageView(locale);
    }

    @RequestMapping(value = EfcSimpleConstants.PLUGIN_IDENTIFIER + "/performUpload", method = RequestMethod.POST)
    @ResponseBody
    public final String handleSubiektUpload(final HttpServletRequest request, @RequestParam("file") final MultipartFile file,
            final Locale locale) {
        return handleUpload(request, file, locale);
    }

    @RequestMapping(value = EfcSimpleConstants.PLUGIN_IDENTIFIER + "/infoPage", method = RequestMethod.GET)
    public final ModelAndView getSubiektInfoPageView(@RequestParam("type") final String type,
            @RequestParam("status") final String status,
            @RequestParam(value = "errorType", required = false) final String errorType,
            @RequestParam(value = "line", required = false) final Integer line,
            @RequestParam(value = "arg", required = false) final String[] args, final Locale locale) {
        return getInfoPageView(type, status, errorType, line, args, locale);
    }

    @Override
    protected IntegrationPerformer getIntegrationPerformer() {
        return integrationPerformer;
    }

    @Override
    protected List<String> getAcceptedFileExtension() {
        return Collections.<String> singletonList("xml");
    }

}
