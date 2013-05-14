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
package com.qcadoo.mes.integration.cfcSimple;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.view.api.crud.CrudService;

public abstract class IntegrationController {

    @Autowired
    private CrudService crudController;

    @Autowired
    private TranslationService translationService;

    protected final ModelAndView getUploadPageView(final Locale locale) {

        ModelAndView mav = getCrudPopupView("integrationFileUpload", locale);

        mav.addObject("headerLabel", getTranslation("uploadView.header", locale));
        mav.addObject("buttonLabel", getTranslation("uploadView.button", locale));
        mav.addObject("chooseFileLabel", getTranslation("uploadView.chooseFileLabel", locale));

        mav.addObject("acceptExtension", getAcceptedFileExtension());
        mav.addObject("submitPage", getIntegrationPerformer().getPluginIdentifier() + "/performUpload.html");

        return mav;
    }

    protected final String handleUpload(final HttpServletRequest request, final MultipartFile file, final Locale locale) {
        try {
            String redirect = getIntegrationPerformer().performImport(file.getInputStream());
            String context = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            return redirect.replaceAll("\\$\\{root\\}", context);
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading file", e);
        }
    }

    protected final ModelAndView getInfoPageView(final String type, final String status, final String errorType,
            final Integer line, final String[] args, final Locale locale) {
        ModelAndView mav = getCrudPopupView("integrationInfoPage", locale);

        if ("success".equals(type)) {
            mav.addObject("headerClass", "successHeader");
            mav.addObject("headerLabel", getTranslation("infoPage.successHeader", locale));

        } else if ("error".equals(type)) {
            mav.addObject("headerClass", "errorHeader");
            mav.addObject("headerLabel", getTranslation("infoPage.errorHeader", locale));
            if ("conversion".equals(errorType)) {
                mav.addObject("contentHeader", getTranslation("infoPage.messages.conversionErrorHeader", locale));
            } else if ("import".equals(errorType)) {
                if (line == null) {
                    mav.addObject("contentHeader", getTranslation("infoPage.messages.parseErrorHeader", locale));
                } else {
                    mav.addObject("contentHeader",
                            getTranslation("infoPage.messages.parseErrorHeaderWithLine", locale, line.toString()));
                }
            }

        } else {
            throw new IllegalStateException("Unsuported info type: " + type);
        }

        mav.addObject("content", getTranslation("infoPage.messages." + status, locale, args));
        return mav;
    }

    private ModelAndView getCrudPopupView(final String viewName, final Locale locale) {
        Map<String, String> crudArgs = new HashMap<String, String>();
        crudArgs.put("popup", "true");
        return crudController.prepareView("cfcSimple", viewName, crudArgs, locale);
    }

    private String getTranslation(final String suffix, final Locale locale, final String... args) {
        return translationService.translate(getIntegrationPerformer().getPluginIdentifier() + "." + suffix,
                "cfcSimple." + suffix, locale, args);
    }

    protected abstract IntegrationPerformer getIntegrationPerformer();

    protected abstract List<String> getAcceptedFileExtension();
}
