/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.portal.controller;

import java.util.Date;

import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.portal.bean.CustomTweetVO;
import com.portal.helper.TwitterViewHelper;

@Controller
@RequestMapping("VIEW")
public class TwitterViewController {
	
	private static final Log LOG = LogFactoryUtil.getLog(TwitterViewController.class);
	
	@Autowired
	private TwitterViewHelper twitterViewHelper;
	
	@Autowired
	private CustomTweetVO customTweetVO;
	
	
	
	public TwitterViewHelper getTwitterViewHelper() {
		return twitterViewHelper;
	}

	public void setTwitterViewHelper(TwitterViewHelper twitterViewHelper) {
		this.twitterViewHelper = twitterViewHelper;
	}

	@RenderMapping
	public String showView(Model model,RenderRequest request,RenderResponse response) {
		try{
			PortletSession portletSession = request.getPortletSession();
			
			/* start current time */
			Date currentDate = new Date();
			long currentTime=System.currentTimeMillis();
			LOG.info("currentTime-->"+currentTime);
			LOG.info("currentDate-->"+currentDate);
			long previousApiCallTime = 0L;
			if(portletSession.getAttribute("apiCallTime") != null){
				previousApiCallTime = (long)portletSession.getAttribute("apiCallTime");
				LOG.info("previousApiCallTime-->"+previousApiCallTime);
			}
			
			long diff = 0L;
			long diffMinutes = 0L;
			if(previousApiCallTime != 0L){
				diff=currentTime-previousApiCallTime;
				diffMinutes = diff / (60 * 1000) % 60;
				LOG.info("diffMinutes-->"+diffMinutes);
			}
			
			/* end current time*/
			
			if(diffMinutes>=30){
				LOG.info("Calling the api in every 30 minutes");
				customTweetVO=twitterViewHelper.fetchTweets();//Fetching the latest tweet
				portletSession.setAttribute("customTweetVO", customTweetVO);
			}
			else if(previousApiCallTime == 0L){
				LOG.info("Calling the api for the first time");
				customTweetVO=twitterViewHelper.fetchTweets();//Fetching the latest tweet	
				portletSession.setAttribute("customTweetVO", customTweetVO);
			}
			else{
				LOG.info("30 minutes not happened,so taking value from session");
				customTweetVO = (CustomTweetVO)portletSession.getAttribute("customTweetVO");
			}
			
			portletSession.setAttribute("apiCallTime", currentTime);
			
			LOG.info("1-->"+portletSession.getAttribute("apiCallTime").toString());
			/*start : Setting values in the model*/
			model.addAttribute("name",customTweetVO.getName());
			model.addAttribute("screenName",customTweetVO.getScreenName());
			model.addAttribute("latestTweet",customTweetVO.getLatestTweet());
			model.addAttribute("profileImageUrl",customTweetVO.getProfileImageUrl());
			model.addAttribute("latestTweetDate",customTweetVO.getLatestTweetDate());
			/*end : Setting values in the model*/
		}
		catch(Exception e){
			LOG.error("Some error occurred while fetching the tweets "+e);
		}
		
		
		LOG.info("latestTweet "+customTweetVO.getLatestTweet());
		return "TwitterBook/view";
	}

}