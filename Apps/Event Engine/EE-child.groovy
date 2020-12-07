/**
*  **************** Event Engine Cog ****************
*
*  Design Usage:
*  Automate your world with easy to use Cogs. Rev up complex automations with just a few clicks!
*
*  Copyright 2020 Bryan Turcotte (@bptworld)
* 
*  This App is free. If you like and use this app, please be sure to mention it on the Hubitat forums! Thanks.
*
*  Remember...I am not a programmer, everything I do takes a lot of time and research!
*  Donations are never necessary but always appreciated. Donations to support development efforts are accepted via: 
*
*  Paypal at: https://paypal.me/bptworld
* 
*  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
*  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
*  the community grow. Have FUN with it!
* 
*-------------------------------------------------------------------------------------------------------------------
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
* ------------------------------------------------------------------------------------------------------------------------------
*
*  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! - @BPTWorld
*
*  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
*
* ------------------------------------------------------------------------------------------------------------------------------
*
*  Changes:
*
*  2.4.4 - 12/06/20 - Added new option to Mode - Use Mode as a Condition but NOT as a Trigger
*  2.4.3 - 12/06/20 - Added 'Condition Helpers', added a 'Dim Warning option' to 'Reverse with Delay', fix for %time%
*  2.4.2 - 12/05/20 - Added a second 'Reverse' cron option
*  2.4.1 - 12/04/20 - Minor changes
*  2.4.0 - 12/03/20 - Code cleanup, added 'Switches In Sequence' Action
*  ---
*  1.0.0 - 09/05/20 - Initial release.
*/

/*
- Working on Send HTTP in Actions - 1175
- Working on Clone Cog - 92
- Working on making map of settings
*/

import groovy.json.*
import hubitat.helper.RMUtils
import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Event Engine"
    state.version = "2.4.4"
}

definition(
    name: "Event Engine Cog",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Automate your world with easy to use Cogs. Rev up complex automations with just a few clicks!",
    category: "Convenience",
    parent: "BPTWorld:Event Engine",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Event%20Engine/EE-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page name: "notificationOptions", title: "", install:false, uninstall:true, nextPage: "pageConfig"
    page name: "copyCogOptions", title: "", install:false, uninstall:true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install:true, uninstall:true, refreshInterval:0) {
        display() 
        state.theCogTriggers = "<b><u>Conditions</u></b><br>"
        section("Instructions:", hideable:true, hidden:true) {
            paragraph "<b>Notes:</b>"
            paragraph "Automate your world with easy to use Cogs. Rev up complex automations with just a few clicks!"
            paragraph "Please <a href='https://docs.google.com/document/d/1QtIsAKUb9vzAZ1RWTQR2SfxaZFvuLO3ePxDxmH-VU48/edit?usp=sharing' target='_blank'>visit this link</a> for a breakdown of all Event Engine Options. (Google Docs)"
        }
        /* if(triggerType == null || triggerType == "") {
            section() {
                href "copyCogOptions", title: "Copy Cog Options", description: "Click here for options"
            }
        } */
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Conditions")) {
            input "triggerType", "enum", title: "Condition Type", options: [
                ["tTimeDays":"Time/Days/Mode - Sub-Menu"],
                ["xAcceleration":"Acceleration Sensor"],
                ["xBattery":"Battery Setpoint"],
                ["xContact":"Contact Sensors"],
                ["xEnergy":"Energy Setpoint"],
                ["xGarageDoor":"Garage Doors"],
                ["xGVar":"Global Variables"],
                ["xHSMAlert":"HSM Alerts *** not tested ***"],
                ["xHSMStatus":"HSM Status *** not tested ***"],
                ["xHumidity":"Humidity Setpoint"],
                ["xIlluminance":"Illuminance Setpoint"],
                ["xLock":"Locks"],
                ["xMotion":"Motion Sensors"], 
                ["xPower":"Power Setpoint"],
                ["xPresence":"Presence Sensor"],
                ["xSwitch":"Switches"],
                ["xTemp":"Temperature Setpoint"],
                ["xTherm":"Thermostat Activity"],
                ["xVoltage":"Voltage Setpoint"],
                ["xWater":"Water Sensor"],
                ["xCustom":"** Custom Attribute **"]
            ], required:false, multiple:true, submitOnChange:true, width:6

            if(triggerType == null) triggerType = ""
            if(timeDaysType == null) timeDaysType = ""
//state.mySettings = [:]
            if(state.mySettings == null) state.mySettings = [:]
            if(triggerType != "") {
                theData = "enum;${triggerType}"
                state.mySettings.put("triggerType",theData)
            } else {
                state.mySettings.remove("triggerType")
            }
            app.updateSetting("mySettings",[value:"${state.mySettings}",type:"text"])
            
            if(triggerType.contains("tTimeDays")) {
                input "timeDaysType", "enum", title: "Time/Days/Mode - Sub-Menu", options: [
                    ["tBetween":"Between Two Times"],
                    ["tMode":"By Mode"],
                    ["tDays":"By Days"],
                    ["tTime":"Certain Time"],
                    ["tSunrise":"Just Sunrise"],
                    ["tSunset":"Just Sunset"],
                    ["tPeriodic":"Periodic Expression"],
                    ["tSunsetSunrise":"Sunset to Sunrise"]
                ], required:true, multiple:true, submitOnChange:true, width:6
                paragraph "<hr>"
            } else {
                paragraph " ", width:6
                app.removeSetting("timeDaysType")
            }

            if(timeDaysType != "") {
                theData = "enum;${timeDaysType}"
                state.mySettings.put("timeDaysType",theData)
            } else {
                state.mySettings.remove("timeDaysType")
            }
            app.updateSetting("mySettings",[value:"${state.mySettings}",type:"text"])
            
            input "triggerAndOr", "bool", title: "Use 'AND' or 'OR' between Condition types", description: "andOr", defaultValue:false, submitOnChange:true, width:12
            if(triggerAndOr) {
                theData = "bool;${triggerAndOr}"
                state.mySettings.put("triggerAndOr",theData)
                paragraph "Cog will fire when <b>ANY</b> Condition is true"
                state.theCogTriggers -= "<b>*</b> Cog will fire when <b>ALL</b> Condition are true (Using AND)<br>"
                state.theCogTriggers += "<b>*</b> Cog will fire when <b>ANY</b> Condition is true (Using OR)<br>"
            } else {
                theData = "bool;${triggerAndOr}"
                state.mySettings.put("triggerAndOr",theData)
                paragraph "Cog will fire when <b>ALL</b> Conditions are true"
                state.theCogTriggers -= "<b>*</b> Cog will fire when <b>ANY</b> Condition is true (Using OR)<br>"
                state.theCogTriggers += "<b>*</b> Cog will fire when <b>ALL</b> Condition are true (Using AND)<br>"
            }
            paragraph "<small>* Excluding any Time/Days/Mode selections.</small>"
            paragraph "<hr>"
            
            app.updateSetting("mySettings",[value:"${state.mySettings}",type:"text"])
            if(testLogEnable) log.info "mySettings: ${mySettings}"
            
// -----------
            if(timeDaysType.contains("tPeriodic")) {
                paragraph "<b>By Periodic</b>"
                input "preMadePeriodic", "text", title: "Enter in a Periodic Cron Expression to 'Run the Cog'", required:false, submitOnChange:true
                paragraph "In addtion to setting up an Expression to start the Cog, you can enter in a second Expression to 'Reverse the Cog'. ie. When the Cog is first run, it turns on a light.  When the second expression is triggered, it will turn the light off."
                input "preMadePeriodic2", "text", title: "Enter in a Periodic Cron Expression to 'Reverse the Cog' (optional)", required:false, submitOnChange:true
                paragraph "Premade cron expressions can be found at <a href='https://www.freeformatter.com/cron-expression-generator-quartz.html#' target='_blank'>this link</a>. Remember, Format and spacing is critical."
                paragraph "Or create your own Expressions locally using the 'Periodic Expressions' app found in Hubitat Package Manager or on <a href='https://github.com/bptworld/Hubitat/' target='_blank'>my GitHub</a>."
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By Periodic - Run: ${preMadePeriodic} - Reverse: ${preMadePeriodic2}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By Periodic - Run: ${preMadePeriodic} - Reverse: ${preMadePeriodic2}<br>"
                app.removeSetting("preMadePeriodic")
            }
// -----------
            if(timeDaysType.contains("tMode")) {
                paragraph "<b>By Mode</b>"
                input "modeEvent", "mode", title: "By Mode", multiple:true, submitOnChange:true
                paragraph "By Mode can also be used as a Restriction. If used as a Restriction, Reverse and Permanent Dim will not run while this Condition is false."
                input "modeMatchRestriction", "bool", defaultValue:false, title: "By Mode as Restriction", description: "By Mode Restriction", submitOnChange:true
                input "modeMatchConditionOnly", "bool", defaultValue:false, title: "Use Mode as a Condition but NOT as a Trigger", description: "Cond Only", submitOnChange:true
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By Mode - ${modeEvent} - as Restriction: ${modeMatchRestriction} - just Condition: ${modeMatchConditionOnly}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By Mode - ${modeEvent} - as Restriction: ${modeMatchRestriction} - just Condition: ${modeMatchConditionOnly}<br>"
                app.removeSetting("modeEvent")
                app.updateSetting("modeMatchRestriction",[value:"false",type:"bool"])
                app.updateSetting("modeMatchConditionOnly",[value:"false",type:"bool"])
            }
// -----------
            if(timeDaysType.contains("tDays")) {
                paragraph "<b>By Days</b>"
                input "days", "enum", title: "Activate on these days", description: "Days to Activate", required:false, multiple:true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
                paragraph "By Days can also be used as a Restriction. If used as a Restriction, Reverse and Permanent Dim will not run while this Condition is false."
                input "daysMatchRestriction", "bool", defaultValue:false, title: "By Days as Restriction", description: "By Days Restriction", submitOnChange:true
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By Days - ${days} - as Restriction: ${daysMatchRestriction}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By Days - ${days} - as Restriction: ${daysMatchRestriction}<br>"
                app.removeSetting("days")
                app.updateSetting("daysMatchRestriction",[value:"false",type:"bool"])
            }
// -----------
            if(timeDaysType.contains("tTime")) {
                paragraph "<b>Certain Time</b>"
                input "startTime", "time", title: "Time to activate", description: "Time", required:false, width:12
                input "repeat", "bool", title: "Repeat", description: "Repeat", defaultValue:false, submitOnChange:true
                if(repeat) {
                    input "repeatType", "enum", title: "Repeat schedule", options: [
                        ["r1min":"1 Minute"],
                        ["r5min":"5 Minutes"],
                        ["r10min":"10 Minutes"],
                        ["r15min":"15 Minutes"],
                        ["r30min":"30 Minutes"],
                        ["r1hour":"1 Hour"],
                        ["r3hour":"3 Hours"]
                    ], required:true, multiple:true, submitOnChange:true
                }
                paragraph "<hr>"
                if(startTime) theDate = toDateTime(startTime)
                state.theCogTriggers += "<b>-</b> Certain Time - ${theDate} - Repeat: ${repeat} - Schedule: ${repeatType}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> Certain Time - ${theDate} - Repeat: ${repeat} - Schedule: ${repeatType}<br>"
                app.removeSetting("startTime")
                app.removeSetting("repeat")
                app.removeSetting("repeatType")
                app.updateSetting("repeat",[value:"false",type:"bool"])
            }
// -----------
            if(timeDaysType.contains("tBetween")) {
                paragraph "<b>Between two times</b>"
                input "fromTime", "time", title: "From", required:false, width: 6, submitOnChange:true
                input "toTime", "time", title: "To", required:false, width: 6
                input "midnightCheckR", "bool", title: "Does this time frame cross over midnight", defaultValue:false, submitOnChange:true
                paragraph "Between two times can also be used as a Restriction. If used as a Restriction, Reverse and Permanent Dim will not run while this Condition is false."
                input "timeBetweenRestriction", "bool", defaultValue:false, title: "Between two times as Restriction", description: "Between two times Restriction", submitOnChange:true
                paragraph "<hr>"
                if(fromTime) theDate1 = toDateTime(fromTime)
                if(midnightCheckR) {
                    if(toTime) theDate2 = toDateTime(toTime)+1
                } else {
                    if(toTime) theDate2 = toDateTime(toTime)
                }
                state.theCogTriggers += "<b>-</b> Between two times - From: ${theDate1} - To: ${theDate2} - Midnight: ${midnightCheckR} - as Restriction: ${timeBetweenRestriction}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> Between two times - From: ${theDate1} - To: ${theDate2} - Midnight: ${midnightCheckR} - as Restriction: ${timeBetweenRestriction}<br>"
                app.removeSetting("fromTime")
                app.removeSetting("toTime")
                app.updateSetting("midnightCheckR",[value:"false",type:"bool"])
                app.updateSetting("timeBetweenRestriction",[value:"false",type:"bool"])
            }
// -----------
            if(timeDaysType.contains("tSunsetSunrise")) {
                if(timeDaysType.contains("tSunsetSunrise") && timeDaysType.contains("tSunrise")) {
                    paragraph "<b>'Sunset to Sunrise' and 'Just Sunrise' can't be used at the same time. Please deselect one of them.</b>"
                } else if(timeDaysType.contains("tSunsetSunrise") && timeDaysType.contains("tSunset")) {
                    paragraph "<b>'Sunset to Sunrise' and 'Just Sunset' can't be used at the same time. Please deselect one of them.</b>"
                } else {
                    paragraph "<b>Sunset to Sunrise</b>" 
                    paragraph "Sunset"
                    input "setBeforeAfter", "bool", title: "Before (off) or After (on) Sunset", defaultValue:false, submitOnChange:true, width:6
                    input "offsetSunset", "number", title: "Offset (minutes)", width:6
                    paragraph "Sunrise"
                    input "riseBeforeAfter", "bool", title: "Before (off) or After (on) Sunrise", defaultValue:false, submitOnChange:true, width:6
                    input "offsetSunrise", "number", title: "Offset(minutes)", width:6
                    paragraph "<small>* Be sure offsets don't cause the time to cross back and forth over midnight or this won't work as expected.</small>"
                    paragraph "Sunset to Sunrise can also be used as a Restriction. If used as a Restriction, Reverse and Permanent Dim will not run while this Condition is false."
                    input "timeBetweenSunRestriction", "bool", defaultValue:false, title: "Sunset to Sunrise as Restriction", description: "Sunset to Sunrise Restriction", submitOnChange:true
                    paragraph "<hr>"
                    state.theCogTriggers += "<b>-</b> Sunset to Sunrise - Sunset Offset: ${offsetSunset}, BeforeAfter: ${setBeforeAfter} - Sunrise Offset: ${offsetSunrise}, BeforeAfter: ${riseBeforeAfter} - with Restriction: ${timeBetweenSunRestriction}<br>"
                }
            } else {
                state.theCogTriggers -= "<b>-</b> Sunset to Sunrise - Sunset Offset: ${offsetSunset}, BeforeAfter: ${setBeforeAfter} - Sunrise Offset: ${offsetSunrise}, BeforeAfter: ${riseBeforeAfter} - with Restriction: ${timeBetweenSunRestriction}<br>"
                app.updateSetting("timeBetweenSunRestriction",[value:"false",type:"bool"])
            }
// -----------
            if(timeDaysType.contains("tSunrise") && timeDaysType.contains("tSunset")) {
                paragraph "<b>Please select 'Sunset to Sunrise', instead of both 'Just Sunrise' and 'Just Sunset'.</b>"
            } else if(timeDaysType.contains("tSunsetSunrise") && timeDaysType.contains("tSunrise")) {
                // Messge above will show
            } else if(timeDaysType.contains("tSunsetSunrise") && timeDaysType.contains("tSunset")) {
                // Messge above will show
            } else if(timeDaysType.contains("tSunrise")) {
                paragraph "<b>Just Sunrise</b>"
                input "riseBeforeAfter", "bool", title: "Before (off) or After (on) Sunrise", defaultValue:false, submitOnChange:true, width:6
                input "offsetSunrise", "number", title: "Offset (minutes)", width:6
                input "sunriseToTime", "bool", title: "Set a certain time to turn off", defaultValue:false, submitOnChange:true
                if(sunriseToTime) {
                    input "sunriseEndTime", "time", title: "Time to End", description: "Time", required:false
                    paragraph "<small>* Must be BEFORE midnight.</small>"
                } else {
                    app.removeSetting("sunriseEndTime")
                }
                paragraph "<hr>"
                if(sunriseEndTime) theDate = toDateTime(sunriseEndTime)
                state.theCogTriggers += "<b>-</b> Just Sunrise - Sunrise Offset: ${offsetSunrise}, BeforeAfter: ${riseBeforeAfter} - Time to End: ${theDate}<br>"
            } else if(timeDaysType.contains("tSunset")) {
                state.theCogTriggers -= "<b>-</b> Just Sunrise - Sunrise Offset: ${offsetSunrise}, BeforeAfter: ${riseBeforeAfter} - Time to End: ${theDate}<br>"
                paragraph "<b>Just Sunset</b>"
                input "setBeforeAfter", "bool", title: "Before (off) or After (on) Sunset", defaultValue:false, submitOnChange:true, width:6
                input "offsetSunset", "number", title: "Offset (minutes)", width:6
                input "sunsetToTime", "bool", title: "Set a certain time to turn off", defaultValue:false, submitOnChange:true
                if(sunsetToTime) {
                    input "sunsetEndTime", "time", title: "Time to End", description: "Time", required:false
                    paragraph "<small>* Must be BEFORE midnight.</small>"
                } else {
                    app.removeSetting("sunsetEndTime")
                }
                paragraph "<hr>"
                if(sunsetEndTime) theDate = toDateTime(sunsetEndTime)
                state.theCogTriggers += "<b>-</b> Just Sunset - Sunset Offset: ${offsetSunset}, BeforeAfter: ${setBeforeAfter} - Time to End: ${theDate}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> Just Sunset - Sunset Offset: ${offsetSunset}, BeforeAfter: ${setBeforeAfter} - Time to End: ${theDate}<br>"
                app.removeSetting("sunsetEndTime")
            }

            if(!timeDaysType.contains("tSunsetSunrise") && !timeDaysType.contains("tSunrise") && !timeDaysType.contains("tSunset")) {
                app.removeSetting("offsetSunrise")
                app.removeSetting("offsetSunset")
                app.updateSetting("setBeforeAfter",[value:"false",type:"bool"])
                app.updateSetting("riseBeforeAfter",[value:"false",type:"bool"])
            }
// -----------
            if(triggerType.contains("xAcceleration")) {
                paragraph "<b>Acceleration Sensor</b>"
                input "accelerationEvent", "capability.accelerationSensor", title: "By Acceleration Sensor", required:false, multiple:true, submitOnChange:true
                if(accelerationEvent) {
                    input "asInactiveActive", "bool", title: "Condition when Inactive (off) or Active (on)", description: "Acceleration", defaultValue:false, submitOnChange:true
                    if(asInactiveActive) {
                        paragraph "Condition true when Sensor(s) becomes Active"
                    } else {
                        paragraph "Condition true when Sensor(s) becomes Inactive"
                    }
                    input "accelerationANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(accelerationANDOR) {
                        paragraph "Condition true when <b>any</b> Acceleration Sensor is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Acceleration Sensors are true"
                    }
                    state.theCogTriggers += "<b>-</b> By Acceleration Sensor: ${accelerationEvent} - InactiveActive: ${asInactiveActive}, ANDOR: ${accelerationANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> By Acceleration Sensor: ${accelerationEvent} - InactiveActive: ${asInactiveActive}, ANDOR: ${accelerationANDOR}<br>"
                    app.removeSetting("accelerationEvent")
                    app.updateSetting("asInactiveActive",[value:"false",type:"bool"])
                    app.updateSetting("accelerationANDOR",[value:"false",type:"bool"])
                }

                input "accelerationRestrictionEvent", "capability.accelerationSensor", title: "Restrict By Acceleration Sensor", required:false, multiple:true, submitOnChange:true
                if(accelerationRestrictionEvent) {
                    input "arInactiveActive", "bool", title: "Restrict when Inactive (off) or Active (on)", description: "Acceleration", defaultValue:false, submitOnChange:true
                    if(arInactiveActive) {
                        paragraph "Restrict when Sensor(s) becomes Active"
                    } else {
                        paragraph "Restrict when Sensor(s) becomes Inactive"
                    }
                    input "accelerationRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(accelerationRANDOR) {
                        paragraph "Restrict when <b>any</b> Acceleration Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Acceleration Sensors are true"
                    }
                    state.theCogTriggers += "<b>Restriction:</b> By Acceleration Sensor: ${accelerationRestrictionEvent} - InactiveActive: ${arInactiveActive}, ANDOR: ${accelerationRANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>Restriction:</b> By Acceleration Sensor: ${accelerationRestrictionEvent} - InactiveActive: ${arInactiveActive}, ANDOR: ${accelerationRANDOR}<br>"
                    app.removeSetting("accelerationRestrictionEvent")
                    app.updateSetting("arInactiveActive",[value:"false",type:"bool"])
                    app.updateSetting("accelerationRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("accelerationEvent")
                app.removeSetting("accelerationRestrictionEvent")
            }
// -----------
            if(triggerType.contains("xBattery")) {
                paragraph "<b>Battery</b>"
                input "batteryEvent", "capability.battery", title: "By Battery Setpoints", required:false, multiple:true, submitOnChange:true
                if(batteryEvent) {
                    input "setBEPointHigh", "bool", defaultValue:false, title: "Condition true when Battery is too High", description: "Battery High", submitOnChange:true
                    if(setBEPointHigh) {
                        input "beSetPointHigh", "number", title: "Battery High Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("beSetPointHigh")
                    }
                    input "setBEPointLow", "bool", defaultValue:false, title: "Condition true when Battery is too Low", description: "Battery Low", submitOnChange:true
                    if(setBEPointLow) {
                        input "beSetPointLow", "number", title: "Battery Low Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("beSetPointLow")
                    }
                    if(beSetPointHigh) paragraph "Cog Will trigger when Battery reading is above or equal to ${beSetPointHigh}"
                    if(beSetPointLow) paragraph "Cog will trigger when Battery reading is below ${beSetPointLow}"
                }
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By Battery Setpoints: ${batteryEvent} - setpoint High: ${setBEPointHigh} ${beSetPointHigh}, setpoint Low: ${setBEPointLow} ${beSetPointLow}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By Battery Setpoints: ${batteryEvent} - setpoint High: ${setBEPointHigh} ${beSetPointHigh}, setpoint Low: ${setBEPointLow} ${beSetPointLow}<br>"
                app.removeSetting("batteryEvent")
                app.removeSetting("beSetPointHigh")
                app.removeSetting("beSetPointLow")
                app.updateSetting("setBEPointHigh",[value:"false",type:"bool"])
                app.updateSetting("setBEPointLow",[value:"false",type:"bool"])
            }
// -----------
            if(triggerType.contains("xContact")) {
                paragraph "<b>Contact</b>"
                input "contactEvent", "capability.contactSensor", title: "By Contact Sensor", required:false, multiple:true, submitOnChange:true
                if(contactEvent) {
                    input "csClosedOpen", "bool", title: "Condition true when Closed (off) or Opened (on)", description: "Contact", defaultValue:false, submitOnChange:true
                    if(csClosedOpen) {
                        paragraph "Condition true when Sensor(s) become Open"
                    } else {
                        paragraph "Condition true when Sensor(s) become Closed"
                    }
                    input "contactANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(contactANDOR) {
                        paragraph "Condition true when <b>any</b> Contact Sensor is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Contact Sensors are true"
                    }
                    state.theCogTriggers += "<b>-</b> By Contact Sensor: ${contactEvent} - ClosedOpen: ${csClosedOpen}, ANDOR: ${contactANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> By Contact Sensor: ${contactEvent} - ClosedOpen: ${csClosedOpen}, ANDOR: ${contactANDOR}<br>"
                    app.removeSetting("contactEvent")
                    app.updateSetting("csClosedOpen",[value:"false",type:"bool"])
                    app.updateSetting("contactANDOR",[value:"false",type:"bool"])
                }
                
                if(contactEvent) {
                    theList = []
                    contactEvent.each { ids ->
                        if(testLogEnable) log.info "Working on $contactEvent"
                        theId = ids.id
                        theList << theId
                    }
                    theData = "capability;${theList};${csClosedOpen};${contactANDOR}"
                    if(testLogEnable) log.info "theData: $theData"
                    state.mySettings.put("contactEvent",theData)
                } else {
                    state.mySettings.remove("contactEvent")
                }
                app.updateSetting("mySettings",[value:"${state.mySettings}",type:"text"])

                input "contactRestrictionEvent", "capability.contactSensor", title: "Restrict By Contact Sensor", required:false, multiple:true, submitOnChange:true
                if(contactRestrictionEvent) {
                    input "crClosedOpen", "bool", title: "Restrict when Closed (off) or Opened (on)", description: "Contact", defaultValue:false, submitOnChange:true
                    if(crClosedOpen) {
                        paragraph "Restrict when Sensor(s) become Open"
                    } else {
                        paragraph "Restrict when Sensor(s) become Closed"
                    }
                    input "contactRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(contactRANDOR) {
                        paragraph "Restrict when <b>any</b> Contact Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Contact Sensors are true"
                    }
                    state.theCogTriggers += "<b>Restriction:</b> By Contact Sensor: ${contactRestrictionEvent} - ClosedOpen: ${crClosedOpen}, ANDOR: ${contactRANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>Restriction:</b> By Contact Sensor: ${contactRestrictionEvent} - ClosedOpen: ${crClosedOpen}, ANDOR: ${contactRANDOR}<br>"
                    app.removeSetting("contactRestrictionEvent")
                    app.updateSetting("crClosedOpen",[value:"false",type:"bool"])
                    app.updateSetting("contactRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("contactEvent")
                app.removeSetting("contactRestrictionEvent")
            }
// -----------
            if(triggerType.contains("xEnergy")) {
                paragraph "<b>Energy</b>"
                input "energyEvent", "capability.powerMeter", title: "By Energy Setpoints", required:false, multiple:true, submitOnChange:true
                if(energyEvent) {
                    input "setEEPointHigh", "bool", defaultValue: "false", title: "Condition true when Energy is too High", description: "Energy High", submitOnChange:true
                    if(setEEPointHigh) {
                        input "eeSetPointHigh", "number", title: "Energy High Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("eeSetPointHigh")
                    }
                    input "setEEPointLow", "bool", defaultValue:false, title: "Condition true when Energy is too Low", description: "Energy Low", submitOnChange:true
                    if(setEEPointLow) {
                        input "eeSetPointLow", "number", title: "Energy Low Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("eeSetPointLow")
                    }
                    if(eeSetPointHigh) paragraph "Cog will trigger when Energy reading is above or equal to ${eeSetPointHigh}"
                    if(eeSetPointLow) paragraph "Cog will trigger when Energy reading is below ${eeSetPointLow}"
                }
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By Energy Setpoints: ${energyEvent} - setpoint High: ${setEEPointHigh} ${eeSetPointHigh}, setpoint Low: ${setEEPointLow} ${eeSetPointLow}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By Energy Setpoints: ${energyEvent} - setpoint High: ${setEEPointHigh} ${eeSetPointHigh}, setpoint Low: ${setEEPointLow} ${eeSetPointLow}<br>"
                app.removeSetting("energyEvent")
                app.removeSetting("eeSetPointHigh")
                app.removeSetting("eeSetPointLow")
                app.updateSetting("setEEPointHigh",[value:"false",type:"bool"])
                app.updateSetting("setEEPointLow",[value:"false",type:"bool"])
            }
// -----------
            if(triggerType.contains("xGarageDoor")) {
                paragraph "<b>Garage Door</b>"
                input "garageDoorEvent", "capability.garageDoorControl", title: "By Garage Door", required:false, multiple:true, submitOnChange:true
                if(garageDoorEvent) {
                    input "gdClosedOpen", "bool", title: "Condition true when Closed (off) or Open (on)", description: "Garage Door", defaultValue:false, submitOnChange:true
                    if(gdClosedOpen) {
                        paragraph "Condition true when Sensor(s) become Open"
                    } else {
                        paragraph "Condition true when Sensor(s) become Closed"
                    }
                    input "garageDoorANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(garageDoorANDOR) {
                        paragraph "Condition true when <b>any</b> Garage Door is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Garage Doors are true"
                    }
                    state.theCogTriggers += "<b>-</b> By Garage Door: ${garageDoorEvent} - ClosedOpen: ${gdClosedOpen}, ANDOR: ${garageDoorANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> By Garage Door: ${garageDoorEvent} - ClosedOpen: ${gdClosedOpen}, ANDOR: ${garageDoorANDOR}<br>"
                    app.removeSetting("garageDoorEvent")
                    app.updateSetting("gdClosedOpen",[value:"false",type:"bool"])
                    app.updateSetting("garageDoorANDOR",[value:"false",type:"bool"])
                }

                input "garageDoorRestrictionEvent", "capability.garageDoorControl", title: "Restrict By Garage Door", required:false, multiple:true, submitOnChange:true
                if(garageDoorRestrictionEvent) {
                    input "gdrClosedOpen", "bool", title: "Restrict when Closed (off) or Open (on)", description: "Garage Door", defaultValue:false, submitOnChange:true
                    if(gdrClosedOpen) {
                        paragraph "Restrict when Sensor(s) become Open"
                    } else {
                        paragraph "Restrict when Sensor(s) become Closed"
                    }
                    input "garageDoorRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(garageDoorANDOR) {
                        paragraph "Restrict when <b>any</b> Garage Door is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Garage Doors are true"
                    }
                    state.theCogTriggers += "<b>Restriction:</b> By Garage Door: ${garageDoorRestrictionEvent} - ClosedOpen: ${gdrClosedOpen}, ANDOR: ${garageDoorANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>Restriction:</b> By Garage Door: ${garageDoorRestrictionEvent} - ClosedOpen: ${gdrClosedOpen}, ANDOR: ${garageDoorANDOR}<br>"
                    app.removeSetting("garageDoorRestrictionEvent")
                    app.updateSetting("gdsClosedOpen",[value:"false",type:"bool"])
                    app.updateSetting("garageDoorRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("garageDoorEvent")
                app.removeSetting("garageDoorRestrictionEvent")
            }
// -----------
            if(triggerType.contains("xGVar")) {
                paragraph "<b>Global Variables</b>"
                paragraph "<small>Be sure to setup a Global Variable in the parent app before trying to use this option.</small>"
                if(state.gvMap) {
                    theList = "${state.gvMap.keySet()}".replace("[","").replace("]","").replace(", ", ",")
                    theList2 = theList.split(",")              
                    input "globalVariableEvent", "enum", title: "By Global Variable", options: theList2, submitOnChange:true
                    input "gvStyle", "bool", title: "Use as Text (off) or Number (on)", defaultValue:false, submitOnChange:true
                    if(gvStyle) {
                        if(globalVariableEvent) {
                            input "setGVPointHigh", "bool", defaultValue:false, title: "Condition true when Variable is too High", description: "Variable High", submitOnChange:true
                            if(setGVPointHigh) {
                                input "gvSetPointHigh", "number", title: "Variable High Setpoint", required:true, submitOnChange:true
                            } else {
                                app.removeSetting("gvSetPointHigh")
                            }
                            input "setGVPointLow", "bool", defaultValue:false, title: "Condition true when Variable is too Low", description: "Variable Low", submitOnChange:true
                            if(setGVPointLow) {
                                input "gvSetPointLow", "number", title: "Variable Low Setpoint", required:true, submitOnChange:true
                            } else {
                                app.removeSetting("gvSetPointLow")
                            }
                            if(gvSetPointHigh) paragraph "Cog will trigger when Variable reading is above or equal to ${gvSetPointHigh}"
                            if(gvSetPointLow) paragraph "Cog will trigger when Variable reading is below ${gvSetPointLow}"
                            app.removeSetting("gvValue")
                            state.theCogTriggers -= "<b>-</b> By Global Variable: ${globalVariableEvent} - Value: ${gvValue}<br>"
                            state.theCogTriggers += "<b>-</b> By Global Variable Setpoints: ${globalVariableEvent} - setpoint High: ${setGVPointHigh} ${gvSetPointHigh}, setpoint Low: ${setGVPointLow} ${gvSetPointLow}<br>"
                        }
                    } else {
                        input "gvValue", "text", title: "Value", required:false, submitOnChange:true
                        app.removeSetting("gvSetPointHigh")
                        app.removeSetting("gvSetPointLow")
                        app.updateSetting("setGVPointHigh",[value:"false",type:"bool"])
                        app.updateSetting("setGVPointLow",[value:"false",type:"bool"])
                        state.theCogTriggers -= "<b>-</b> By Global Variable Setpoints: ${globalVariableEvent} - setpoint High: ${setGVPointHigh} ${gvSetPointHigh}, setpoint Low: ${setGVPointLow} ${gvSetPointLow}<br>"
                        state.theCogTriggers += "<b>-</b> By Global Variable: ${globalVariableEvent} - Value: ${gvValue}<br>"
                    }
                } else {
                    paragraph "<b>In order to use the Global Variables, please be sure to do the following</b><br>- Setup at least one Global Variable in the parent app.<br>- This Cog needs to be saved first. Please scroll down and hit 'Done' before continuing. Then open the Cog again.</b>"
                }
                paragraph "<hr>"               
            } else {
                state.theCogTriggers -= "<b>-</b> By Global Variable: ${globalVariableEvent} - Value: ${gvValue}<br>"
                state.theCogTriggers -= "<b>-</b> By Global Variable Setpoints: ${globalVariableEvent} - setpoint High: ${setGVPointHigh} ${gvSetPointHigh}, setpoint Low: ${setGVPointLow} ${gvSetPointLow}<br>"
                app.removeSetting("globalVariableEvent")
                app.removeSetting("gvValue")
                app.removeSetting("gvSetPointHigh")
                app.removeSetting("gvSetPointLow")
                app.updateSetting("setGVPointHigh",[value:"false",type:"bool"])
                app.updateSetting("setGVPointLow",[value:"false",type:"bool"])
            }
// -----------
            if(triggerType.contains("xHSMAlert")) {
                paragraph "<b>HSM Alert</b>"
                paragraph "<b>Warning: This Condition has not been tested. Use at your own risk.</b>"
                input "hsmAlertEvent", "enum", title: "By HSM Alert", options: ["arming", "armingHome", "armingNight", "cancel", "cancelRuleAlerts", "intrusion", "intrusion-delay", "intrusion-home", "intrusion-home-delay", "intrusion-night", "intrusion-night-delay", "rule", "smoke", "water"], multiple:true, submitOnChange:true
                if(hsmAlertEvent) paragraph "Cog will trigger when <b>any</b> of the HSM Alerts are active."
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By HSM Alert: ${hsmAlertEvent}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By HSM Alert: ${hsmAlertEvent}<br>"
                app.removeSetting("hsmAlertEvent")
            }
// -----------
            if(triggerType.contains("xHSMStatus")) {
                paragraph "<b>HSM Status</b>"
                paragraph "<b>Warning: This Condition has not been tested. Use at your own risk.</b>"
                input "hsmStatusEvent", "enum", title: "By HSM Status", options: ["All Disarmed", "Armed Away", "Armed Home", "Armed Night", "Delayed Armed Away", "Delayed Armed Home", "Delayed Armed Night", "Disarmed"], multiple:true, submitOnChange:true
                if(hsmStatusEvent) paragraph "Cog will trigger when <b>any</b> of the HSM Status are active."
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By HSM Status: ${hsmStatusEvent}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By HSM Status: ${hsmStatusEvent}<br>"
                app.removeSetting("hsmStatusEvent")
            }
// -----------
            if(triggerType.contains("xHumidity")) {
                paragraph "<b>Humidity</b>"
                input "humidityEvent", "capability.relativeHumidityMeasurement", title: "By Humidity Setpoints", required:false, multiple:true, submitOnChange:true
                if(humidityEvent) {
                    input "setHEPointHigh", "bool", defaultValue:false, title: "Condition true when Humidity is too High", description: "Humidity High", submitOnChange:true
                    if(setHEPointHigh) {
                        input "heSetPointHigh", "number", title: "Humidity High Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("heSetPointHigh")
                    }
                    input "setHEPointLow", "bool", defaultValue:false, title: "Condition true when Humidity is too Low", description: "Humidity Low", submitOnChange:true
                    if(setHEPointLow) {
                        input "heSetPointLow", "number", title: "Humidity Low Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("heSetPointLow")
                    }
                    if(heSetPointHigh) paragraph "Cog will trigger when Humidity reading is above or equal to ${heSetPointHigh}"
                    if(heSetPointLow) paragraph "Cog will trigger when Humidity reading is below ${heSetPointLow}"
                }
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By Humidity Setpoints: ${humidityEvent} - setpoint High: ${setHEPointHigh} ${seSetPointHigh}, setpoint Low: ${setHEPointLow} ${seSetPointLow}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By Humidity Setpoints: ${humidityEvent} - setpoint High: ${setHEPointHigh} ${seSetPointHigh}, setpoint Low: ${setHEPointLow} ${seSetPointLow}<br>"
                app.removeSetting("humidityEvent")
                app.removeSetting("heSetPointHigh")
                app.removeSetting("heSetPointLow")
                app.updateSetting("setHEPointHigh",[value:"false",type:"bool"])
                app.updateSetting("setHEPointLow",[value:"false",type:"bool"])
            }
// -----------
            if(triggerType.contains("xIlluminance")) {
                paragraph "<b>Illuminance</b>"
                input "illuminanceEvent", "capability.illuminanceMeasurement", title: "By Illuminance Setpoints", required:false, multiple:true, submitOnChange:true
                if(illuminanceEvent) {
                    input "setIEPointHigh", "bool", defaultValue:false, title: "Condition true when Illuminance is too High", description: "High", submitOnChange:true
                    if(setIEPointHigh) {
                        input "ieSetPointHigh", "number", title: "Illuminance High Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("ieSetPointHigh")
                    }
                    input "setIEPointLow", "bool", defaultValue:false, title: "Condition true when Illuminance is too Low", description: "Low", submitOnChange:true
                    if(setIEPointLow) {
                        input "ieSetPointLow", "number", title: "Illuminance Low Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("ieSetPointLow")
                    }
                    if(iSetPointHigh) paragraph "Cog will trigger when Humidity reading is above or equal to ${ieSetPointHigh}"
                    if(iSetPointLow) paragraph "Cog will trigger when Humidity reading is below ${ieSetPointLow}"
                }
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By Illuminance Setpoints: ${illuminanceEvent} - setpoint High: ${setIEPointHigh} ${ieSetPointHigh}, setpoint Low: ${setIEPointLow} ${ieSetPointLow}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By Illuminance Setpoints: ${illuminanceEvent} - setpoint High: ${setIEPointHigh} ${ieSetPointHigh}, setpoint Low: ${setIEPointLow} ${ieSetPointLow}<br>"
                app.removeSetting("illuminanceEvent")
                app.removeSetting("ieSetPointHigh")
                app.removeSetting("ieSetPointLow")
                app.updateSetting("setIEPointHigh",[value:"false",type:"bool"])
                app.updateSetting("setIEPointLow",[value:"false",type:"bool"])
            }
// -----------
            if(triggerType.contains("xLock")) {
                paragraph "<b>Lock</b>"
                input "lockEvent", "capability.lock", title: "By Lock", required:false, multiple:true, submitOnChange:true
                if(lockEvent) {
                    input "lUnlockedLocked", "bool", title: "Condition true when Unlocked (off) or Locked (on)", description: "Lock", defaultValue:false, submitOnChange:true
                    if(lUnlockedLocked) {
                        paragraph "Condition true when Sensor(s) become Locked"
                    } else {
                        paragraph "Condition true when Sensor(s) become Unlocked"
                    }
                    input "lockANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(lockANDOR) {
                        paragraph "Condition true when <b>any</b> Lock is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Locks are true"
                    }
                    theNames = getLockCodeNames(lockEvent)
                    input "lockUser", "enum", title: "By Lock User", options: theNames, required:false, multiple:true, submitOnChange:true
                    paragraph "<small>* Note: If you are using HubConnect and have this cog on a different hub than the Lock, the lock codes must not be encrypted.</small>"
                    state.theCogTriggers += "<b>-</b> By Lock: ${lockEvent} - UnlockedLocked: ${lUnlockedLocked}, lockANDOR: ${lockANDOR}, Lock User: ${lockUser}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> By Lock: ${lockEvent} - UnlockedLocked: ${lUnlockedLocked}, lockANDOR: ${lockANDOR}, Lock User: ${lockUser}<br>"
                    app.removeSetting("lockUser")
                    app.removeSetting("lockEvent")
                    app.updateSetting("lUnlockedLocked",[value:"false",type:"bool"])
                    app.updateSetting("lockANDOR",[value:"false",type:"bool"])
                }

                input "lockRestrictionEvent", "capability.lock", title: "Restrict By Lock", required:false, multiple:false, submitOnChange:true
                if(lockRestrictionEvent) {
                    input "lrUnlockedLocked", "bool", title: "Restrict when Unlocked (off) or Locked (on)", description: "Lock", defaultValue:false, submitOnChange:true
                    if(lrUnlockedLocked) {
                        paragraph "Restrict when Sensor(s) become Locked"
                    } else {
                        paragraph "Restrict when Sensor(s) become Unlocked"
                    }
                    input "lockRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(lockRANDOR) {
                        paragraph "Restrict when <b>any</b> Lock is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Locks are true"
                    }
                    theNames = getLockCodeNames(lockRestrictionEvent)
                    input "lockRestrictionUser", "enum", title: "Restrict By Lock User", options: theNames, required:false, multiple:true, submitOnChange:true
                    paragraph "<small>* Note: If you are using HubConnect and have this cog on a different hub than the Lock, the lock codes must not be encryted.</small>"
                    state.theCogTriggers += "<b>Restriction:</b> By Lock: ${lockRestrictionEvent} - UnlockedLocked: ${lrUnlockedLocked}, lock User: ${lockRestrictionUser}<br>"
                } else {
                    state.theCogTriggers -= "<b>Restriction:</b> By Lock: ${lockRestrictionEvent} - UnlockedLocked: ${lrUnlockedLocked}, lock User: ${lockRestrictionUser}<br>"
                    app.removeSetting("lockRestrictionUser")
                    app.removeSetting("lockRestrictionEvent")
                    app.updateSetting("lrUnlockedLocked",[value:"false",type:"bool"])
                    app.updateSetting("lockRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>" 
            } else {
                app.removeSetting("lockEvent")
                app.removeSetting("lockRestrictionEvent")
            }
// -----------
            if(triggerType.contains("xMotion")) {
                paragraph "<b>Motion</b>"
                input "motionEvent", "capability.motionSensor", title: "By Motion Sensor", required:false, multiple:true, submitOnChange:true
                if(motionEvent) {
                    input "meInactiveActive", "bool", defaultValue:false, title: "Motion Inactive (off) or Active (on)", description: "Motion", submitOnChange:true
                    if(meInactiveActive) {
                        paragraph "Condition true when Sensor(s) becomes Active"
                    } else {
                        paragraph "Condition true when Sensor(s) becomes Inactive"
                    }
                    input "motionANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(motionANDOR) {
                        paragraph "Condition true when <b>any</b> Motion Sensor is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Motion Sensors are true"
                    }
                    state.theCogTriggers += "<b>-</b> By Motion Sensor: ${motionEvent} - InactiveActive: ${meInactiveActive}, ANDOR: ${motionANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> By Motion Sensor: ${motionEvent} - InactiveActive: ${meInactiveActive}, ANDOR: ${motionANDOR}<br>"
                    app.removeSetting("motionEvent")
                    app.updateSetting("meInactiveActive",[value:"false",type:"bool"])
                    app.updateSetting("motionANDOR",[value:"false",type:"bool"])
                }

                input "motionRestrictionEvent", "capability.motionSensor", title: "Restrict By Motion Sensor", required:false, multiple:true, submitOnChange:true
                if(motionRestrictionEvent) {
                    input "mrInactiveActive", "bool", defaultValue:false, title: "Motion Inactive (off) or Active (on)", description: "Motion", submitOnChange:true
                    if(mrInactiveActive) {
                        paragraph "Restrict when Sensor(s) becomes Active"
                    } else {
                        paragraph "Restrict when Sensor(s) becomes Inactive"
                    }
                    input "motionRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(motionRANDOR) {
                        paragraph "Restrict when <b>any</b> Motion Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Motion Sensors are true"
                    }
                    state.theCogTriggers += "<b>Restriction:</b> By Motion Sensor: ${motionRestrictionEvent} - InactiveActive: ${mrInactiveActive}, ANDOR: ${motionRANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>Restriction:</b> By Motion Sensor: ${motionRestrictionEvent} - InactiveActive: ${mrInactiveActive}, ANDOR: ${motionRANDOR}<br>"
                    app.removeSetting("motionRestrictionEvent")
                    app.updateSetting("mrInactiveActive",[value:"false",type:"bool"])
                    app.updateSetting("motionRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("motionEvent")
                app.removeSetting("motionRestrictionEvent")
            }
// -----------
            if(triggerType.contains("xPower")) {
                paragraph "<b>Power</b>"
                input "powerEvent", "capability.powerMeter", title: "By Power Setpoints", required:false, multiple:true, submitOnChange:true
                if(powerEvent) {
                    input "setPEPointHigh", "bool", defaultValue: "false", title: "Condition true when Power is too High", description: "Power High", submitOnChange:true
                    if(setPEPointHigh) {
                        input "peSetPointHigh", "number", title: "Power High Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("peSetPointHigh")
                    }

                    input "setPEPointLow", "bool", defaultValue:false, title: "Condition true when Power is too Low", description: "Power Low", submitOnChange:true
                    if(setPEPointLow) {
                        input "peSetPointLow", "number", title: "Power Low Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("peSetPointLow")
                    }

                    if(setPEPointHigh) paragraph "Cog will trigger when Power reading is above or equal to ${peSetPointHigh}"
                    if(setPEPointLow) paragraph "Cog will trigger when Power reading is below ${peSetPointLow}"
                }
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By Power Setpoints: ${powerEvent} - setpoint High: ${setPEPointHigh} ${peSetPointHigh}, setpoint Low: ${setPEPointLow} ${peSetPointLow}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By Power Setpoints: ${powerEvent} - setpoint High: ${setPEPointHigh} ${peSetPointHigh}, setpoint Low: ${setPEPointLow} ${peSetPointLow}<br>"
                app.removeSetting("powerEvent")
                app.removeSetting("peSetPointHigh")
                app.removeSetting("peSetPointLow")
                app.updateSetting("setPEPointHigh",[value:"false",type:"bool"])
                app.updateSetting("setPEPointLow",[value:"false",type:"bool"])
            }
// -----------
            if(triggerType.contains("xPresence")) {
                paragraph "<b>Presence</b>"
                input "presenceEvent", "capability.presenceSensor", title: "By Presence Sensor", required:false, multiple:true, submitOnChange:true
                if(presenceEvent) {
                    input "psPresentNotPresent", "bool", title: "Condition true when Present (off) or Not Present (on)", description: "Present", defaultValue:false, submitOnChange:true
                    if(psPresentNotPresent) {
                        paragraph "Condition true when Sensor(s) become Not Present"
                    } else {
                        paragraph "Condition true when Sensor(s) become Present"
                    }

                    input "presentANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(presentANDOR) {
                        paragraph "Condition true when <b>any</b> Presence Sensor is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Presence Sensors are true"
                    }
                    state.theCogTriggers += "<b>-</b> By Presence Sensor: ${presenceEvent} - PresentNotPresent: ${psPresentNotPresent}, ANDOR: ${presentANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> By Presence Sensor: ${presenceEvent} - PresentNotPresent: ${psPresentNotPresent}, ANDOR: ${presentANDOR}<br>"
                    app.removeSetting("presenceEvent")
                    app.updateSetting("psPresentNotPresent",[value:"false",type:"bool"])
                    app.updateSetting("presentANDOR",[value:"false",type:"bool"])
                }

                input "presenceRestrictionEvent", "capability.presenceSensor", title: "Restrict By Presence Sensor", required:false, multiple:true, submitOnChange:true
                if(presenceRestrictionEvent) {
                    input "prPresentNotPresent", "bool", title: "Restrict when Present (off) or Not Present (on)", description: "Present", defaultValue:false, submitOnChange:true
                    if(prPresentNotPresent) {
                        paragraph "Restrict when Sensor(s) become Not Present"
                    } else {
                        paragraph "Restrict when Sensor(s) become Present"
                    }

                    input "presentRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(presentRANDOR) {
                        paragraph "Restrict when <b>any</b> Presence Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Presence Sensors are true"
                    }
                    state.theCogTriggers += "<b>Restriction:</b> By Presence Sensor: ${presenceRestrictionEvent} - PresentNotPresent: ${prPresentNotPresent}, ANDOR: ${presentRANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>Restriction:</b> By Presence Sensor: ${presenceRestrictionEvent} - PresentNotPresent: ${prPresentNotPresent}, ANDOR: ${presentRANDOR}<br>"
                    app.removeSetting("presenceRestrictionEvent")
                    app.updateSetting("prPresentNotPresent",[value:"false",type:"bool"])
                    app.updateSetting("presentRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("presenceEvent")
                app.removeSetting("presenceRestrictionEvent")
            }
// -----------
            if(triggerType.contains("xSwitch")) {
                paragraph "<b>Switch</b>"
                input "switchEvent", "capability.switch", title: "By Switch", required:false, multiple:true, submitOnChange:true
                if(switchEvent) {
                    input "seOffOn", "bool", defaultValue:false, title: "Switch Off (off) or On (on)", description: "Switch", submitOnChange:true
                    input "seType", "bool", defaultValue:false, title: "Only when Physically pushed", description: "Switch Type", submitOnChange:true
                    if(seOffOn) {
                        paragraph "Condition true when Sensor(s) becomes On"
                    } else {
                        paragraph "Condition true when Sensor(s) becomes Off"
                    }
                    if(seType) { paragraph "<small>* Event 'Description Text' must contain '[physical]' for this to work. HE stock drivers do, others may vary.</small>" }
                    input "switchANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(switchANDOR) {
                        paragraph "Condition true when <b>any</b> Switch is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Switches are true"
                    }
                    state.theCogTriggers += "<b>-</b> By Switch: ${switchEvent} - OffOn: ${seOffOn}, ANDOR: ${switchANDOR}, Physical: ${seType}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> By Switch: ${switchEvent} - OffOn: ${seOffOn}, ANDOR: ${switchANDOR}, Physical: ${seType}<br>"
                    app.removeSetting("switchEvent")
                    app.updateSetting("seOffOn",[value:"false",type:"bool"])
                    app.updateSetting("switchANDOR",[value:"false",type:"bool"])
                    app.removeSetting("seStateMin")
                    app.updateSetting("seInState",[value:"false",type:"bool"])
                }
// -----------
                input "switchRestrictionEvent", "capability.switch", title: "Restrict by Switch", required:false, multiple:true, submitOnChange:true
                if(switchRestrictionEvent) {
                    input "srOffOn", "bool", defaultValue:false, title: "Switch Off (off) or On (on)", description: "Switch", submitOnChange:true
                    if(srOffOn) {
                        paragraph "Restrict when Switch(es) are On"
                    } else {
                        paragraph "Restrict when Switch(es) are Off"
                    }
                    input "switchRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(switchRANDOR) {
                        paragraph "Restrict when <b>any</b> Switch is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Switches are true"
                    }
                    state.theCogTriggers += "<b>Restriction:</b> By Switch: ${switchRestrictionEvent} - OffOn: ${srOffOn}, ANDOR: ${switchRANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>Restriction:</b> By Switch: ${switchRestrictionEvent} - OffOn: ${srOffOn}, ANDOR: ${switchRANDOR}<br>"
                    app.removeSetting("switchRestrictionEvent")
                    app.updateSetting("srOffOn",[value:"false",type:"bool"])
                    app.updateSetting("switchRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("switchEvent")
                app.removeSetting("switchRestrictionEvent")
            }
// -----------
            if(triggerType.contains("xTemp")) {
                paragraph "<b>Temperature</b>"
                input "tempEvent", "capability.temperatureMeasurement", title: "By Temperature Setpoints", required:false, multiple:true, submitOnChange:true
                if(tempEvent) {
                    input "setTEPointHigh", "bool", defaultValue:false, title: "Condition true when Temperature is too High", description: "Temp High", submitOnChange:true
                    if(setTEPointHigh) {
                        input "teSetPointHigh", "number", title: "Temperature High Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("setTEPointHigh")
                    }
                    input "setTEPointLow", "bool", defaultValue:false, title: "Condition true when Temperature is too Low", description: "Temp Low", submitOnChange:true
                    if(setTEPointLow) {
                        input "teSetPointLow", "number", title: "Temperature Low Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("setTEPointLow")
                    }
                    if(teSetPointHigh) paragraph "Cog will trigger when Temperature reading is above or equal to ${teSetPointHigh}"
                    if(teSetPointLow) paragraph "Cog will trigger when Temperature reading is below ${teSetPointLow}"
                }
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By Temperature Setpoints: ${tempEvent} - setpoint High: ${setTEPointHigh} ${teSetPointHigh}, setpoint Low: ${setTEPointLow} ${teSetPointLow}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By Temperature Setpoints: ${tempEvent} - setpoint High: ${setTEPointHigh} ${teSetPointHigh}, setpoint Low: ${setTEPointLow} ${teSetPointLow}<br>"
                app.removeSetting("tempEvent")
                app.removeSetting("teSetPointHigh")
                app.removeSetting("teSetPointLow")
                app.updateSetting("setTEPointHigh",[value:"false",type:"bool"])
                app.updateSetting("setTEPointLow",[value:"false",type:"bool"])
            }
// -----------            
            if(triggerType.contains("xTherm")) {
                paragraph "<b>Thermostat</b>"
                paragraph "Tracks the state of the thermostat. It will react if not in Idle. (ie. heating or cooling)"
                input "thermoEvent", "capability.thermostat", title: "Thermostat to track", required:false, multiple:true, submitOnChange:true
                input "thermoANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                if(thermoANDOR) {
                    paragraph "Condition true when <b>any</b> Thermostat is true"
                } else {
                    paragraph "Condition true when <b>all</b> Thermostats are true"
                }
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By Thermostat: ${thermoEvent} - ANDOR: ${thermoANDOR}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By Thermostat: ${thermoEvent} - ANDOR: ${thermoANDOR}<br>"
                app.removeSetting("thermoEvent")
            }
// -----------
            if(triggerType.contains("xVoltage")) {
                paragraph "<b>Voltage</b>"
                input "voltageEvent", "capability.voltageMeasurement", title: "By Voltage Setpoints", required:false, multiple:true, submitOnChange:true
                if(voltageEvent) {
                    input "setVEPointHigh", "bool", defaultValue:false, title: "Condition true when Voltage is too High", description: "Voltage High", submitOnChange:true
                    if(setVEPointHigh) {
                        input "veSetPointHigh", "number", title: "Voltage High Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("veSetPointHigh")
                    }
                    input "setVEPointLow", "bool", defaultValue:false, title: "Condition true when Voltage is too Low", description: "Voltage Low", submitOnChange:true
                    if(setVEPointLow) {
                        input "veSetPointLow", "number", title: "Voltage Low Setpoint", required:true, submitOnChange:true
                    } else {
                        app.removeSetting("veSetPointLow")
                    }
                    if(veSetPointHigh) paragraph "Cog will trigger when Voltage reading is above or equal to ${veSetPointHigh}"
                    if(veSetPointLow) paragraph "Cog will trigger when Voltage reading is below ${veSetPointLow}"
                }
                paragraph "<hr>"
                state.theCogTriggers += "<b>-</b> By Voltage Setpoints: ${voltageEvent} - setpoint High: ${setVEPointHigh} ${veSetPointHigh}, setpoint Low: ${setVEPointLow} ${veSetPointLow}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> By Voltage Setpoints: ${voltageEvent} - setpoint High: ${setVEPointHigh} ${veSetPointHigh}, setpoint Low: ${setVEPointLow} ${veSetPointLow}<br>"
                app.removeSetting("voltageEvent")
                app.removeSetting("veSetPointHigh")
                app.removeSetting("veSetPointLow")
                app.updateSetting("setVEPointHigh",[value:"false",type:"bool"])
                app.updateSetting("setVEPointLow",[value:"false",type:"bool"])
            }
// -----------
            if(triggerType.contains("xWater")) {
                paragraph "<b>Water</b>"
                input "waterEvent", "capability.waterSensor", title: "By Water Sensor", required:false, multiple:true, submitOnChange:true
                if(waterEvent) {
                    input "wsDryWet", "bool", title: "Condition true when Dry (off) or Wet (on)", description: "Water", defaultValue:false, submitOnChange:true
                    if(wsDryWet) {
                        paragraph "Condition true when Sensor(s) become Wet"
                    } else {
                        paragraph "Condition true when Sensor(s) become Dry"
                    }
                    input "waterANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(waterANDOR) {
                        paragraph "Condition true when <b>any</b> Water Sensor is true"
                    } else {
                        paragraph "Condition true when <b>all</b> Water Sensors are true"
                    }
                    state.theCogTriggers += "<b>-</b> By Water Sensor: ${waterEvent} - DryWet: ${wsDryWet}, ANDOR: ${waterANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> By Water Sensor: ${waterEvent} - DryWet: ${wsDryWet}, ANDOR: ${waterANDOR}<br>"
                    app.removeSetting("waterEvent")
                    app.updateSetting("wsDryWet",[value:"false",type:"bool"])
                    app.updateSetting("waterANDOR",[value:"false",type:"bool"])
                }

                input "waterRestrictionEvent", "capability.waterSensor", title: "Restrict By Water Sensor", required:false, multiple:true, submitOnChange:true
                if(waterRestrictionEvent) {
                    input "wrDryWet", "bool", title: "Restrict when Dry (off) or Wet (on)", description: "Water", defaultValue:false, submitOnChange:true
                    if(wrDryWet) {
                        paragraph "Restrict when Sensor(s) become Wet"
                    } else {
                        paragraph "Restrict when Sensor(s) become Dry"
                    }
                    input "waterRANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                    if(waterANDOR) {
                        paragraph "Restrict when <b>any</b> Water Sensor is true"
                    } else {
                        paragraph "Restrict when <b>all</b> Water Sensors are true"
                    }
                    state.theCogTriggers += "<b>Restriction:</b> By Water Sensor: ${waterRestrictionEvent} - DryWet: ${wrDryWet}, ANDOR: ${waterANDOR}<br>"
                } else {
                    state.theCogTriggers -= "<b>Restriction:</b> By Water Sensor: ${waterRestrictionEvent} - DryWet: ${wrDryWet}, ANDOR: ${waterANDOR}<br>"
                    app.removeSetting("waterRestrictionEvent")
                    app.updateSetting("wrDryWet",[value:"false",type:"bool"])
                    app.updateSetting("waterRANDOR",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("waterEvent")
                app.removeSetting("waterRestrictionEvent")
            }
// -----------
            if(triggerType.contains("xCustom")) {
                paragraph "<b>Custom Attribute</b>"
                input "customEvent", "capability.*", title: "Select Device(s)", required:false, multiple:true, submitOnChange:true
                if(customEvent) {
                    allAttrs1 = []
                    allAttrs1 = customEvent.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                    allAttrs1a = allAttrs1.sort { a, b -> a.value <=> b.value }
                    input "specialAtt", "enum", title: "Attribute to track", options: allAttrs1a, required:true, multiple:false, submitOnChange:true
                    input "deviceORsetpoint", "bool", defaultValue:false, title: "Device (off) or Setpoint (on)", description: "Whole", submitOnChange:true
                    if(deviceORsetpoint) {
                        input "setSDPointHigh", "bool", defaultValue:false, title: "Condition true when Custom is too High", description: "Custom High", submitOnChange:true
                        if(setSDPointHigh) {
                            input "sdSetPointHigh", "number", title: "Custom High Setpoint", required:true, submitOnChange:true
                        } else {
                            app.removeSetting("sdSetPointHigh")
                        }
                        input "setSDPointLow", "bool", defaultValue:false, title: "Condition true when Custom is too Low", description: "Custom Low", submitOnChange:true
                        if(setSDPointLow) {
                            input "sdSetPointLow", "number", title: "Custom Low Setpoint", required:true, submitOnChange:true
                        } else {
                            app.removeSetting("sdSetPointLow")
                        }
                        if(sdSetPointHigh) paragraph "Cog will trigger when Custom reading is above or equal to ${sdSetPointHigh}"
                        if(sdSetPointLow) paragraph "Cog will trigger when Custom reading is below ${sdSetPointLow}"
                        state.theCogTriggers -= "<b>-</b> By Custom: ${customEvent} - custom1: ${custom1} - custom2: ${custom2} - value1or2: ${sdCustom1Custom2}, ANDOR: ${customANDOR}<br>"
                        state.theCogTriggers += "<b>-</b> By Custom Setpoints: ${customEvent} - setpoint High: ${setSDPointHigh} ${sdSetPointHigh}, setpoint Low: ${setSDPointLow} ${sdSetPointLow}<br>"
                        
                        app.removeSetting("custom1")
                        app.removeSetting("custom2")
                        app.updateSetting("sdCustom1Custom2",[value:"false",type:"bool"])
                        app.updateSetting("customANDOR",[value:"false",type:"bool"])
                    } else {
                        paragraph "Enter in the attribute values required to trigger Cog. Must be exactly as seen in the device current stats. (ie. on/off, open/closed)"
                        input "custom1", "text", title: "Attribute Value 1", required:true, submitOnChange:true
                        input "custom2", "text", title: "Attribute Value 2", required:true, submitOnChange:true
                        input "sdCustom1Custom2", "bool", title: "Condition true when ${custom1} (off) or ${custom2} (on)", description: "Custom", defaultValue:false, submitOnChange:true
                        if(sdCustom1Custom2) {
                            paragraph "Condition true when Custom(s) become ${custom1}"
                        } else {
                            paragraph "Condition true when Custom(s) become ${custom2}"
                        }
                        paragraph "* Remember - If Conditions are working backwards, simply reverse your values above."
                        input "customANDOR", "bool", title: "Use 'AND' (off) or 'OR' (on)", description: "And Or", defaultValue:false, submitOnChange:true
                        if(customANDOR) {
                            paragraph "Condition true when <b>any</b> Custom is true"
                        } else {
                            paragraph "Condition true when <b>all</b> Custom are true"
                        }
                        state.theCogTriggers -= "<b>-</b> By Custom Setpoints: ${customEvent} - setpoint High: ${setSDPointHigh} ${sdSetPointHigh}, setpoint Low: ${setSDPointLow} ${sdSetPointLow}<br>"
                        state.theCogTriggers += "<b>-</b> By Custom: ${customEvent} - custom1: ${custom1} - custom2: ${custom2} - value1or2: ${sdCustom1Custom2}, ANDOR: ${customANDOR}<br>"
                        app.removeSetting("sdSetPointHigh")
                        app.removeSetting("sdSetPointLow")
                        app.updateSetting("setSDPointHigh",[value:"false",type:"bool"])
                        app.updateSetting("setSDPointLow",[value:"false",type:"bool"])
                    }
                }
            } else {
                state.theCogTriggers -= "<b>-</b> By Custom Setpoints: ${customEvent} - setpoint High: ${setSDPointHigh} ${sdSetPointHigh}, setpoint Low: ${setSDPointLow} ${sdSetPointLow}<br>"
                state.theCogTriggers -= "<b>-</b> By Custom: ${customEvent} - custom1: ${custom1} - custom2: ${custom2} - value1or2: ${sdCustom1Custom2}, ANDOR: ${customANDOR}<br>"
                app.removeSetting("customEvent")
                app.removeSetting("specialAtt")
                app.removeSetting("custom1")
                app.removeSetting("custom2")
                app.updateSetting("sdCustom1Custom2",[value:"false",type:"bool"])
                app.updateSetting("customANDOR",[value:"false",type:"bool"])
                app.removeSetting("sdSetPointHigh")
                app.removeSetting("sdSetPointLow")
                app.updateSetting("setSDPointHigh",[value:"false",type:"bool"])
                app.updateSetting("setSDPointLow",[value:"false",type:"bool"])
            }

            if(batteryEvent || humidityEvent || illuminanceEvent || powerEvent || tempEvent || (customEvent && deviceORsetpoint)) {
                input "setpointRollingAverage", "bool", title: "Use a rolling Average", description: "average", defaultValue:false, submitOnChange:true
                if(setpointRollingAverage) {
                    paragraph "<small>*All values are rounded for this option</small>"
                    input "numOfPoints", "number", title: "Number of points to average", required:true, submitOnChange:true
                    app.updateSetting("useWholeNumber",[value:"true",type:"bool"])
                } else {
                    app.removeSetting("numOfPoints")
                    state.readings = []
                }
                input "useWholeNumber", "bool", title: "Only use Whole Numbers (round each number)", description: "Whole", defaultValue:false, submitOnChange:true
                if(setpointRollingAverage) paragraph "<b>When using a Rolling Average, use Whole Numbers MUST also be true.</b>"
                paragraph "<small>* Note: This effects the data coming in from the device.</small>"
                paragraph "Setpoint truths can also be reset one time daily. Typically to allow another notification of a high/low reading."
                input "spResetTime", "time", title: "Time to reset Setpoint truths (optional)", description: "Reset SP", required:false
                state.theCogTriggers += "<b>-</b> Rolling Average: ${setpointRollingAverage} - Use Whole Numbers: ${useWholeNumber} - ResetTime: ${spResetTime}<br>"
            } else {
                state.theCogTriggers -= "<b>-</b> Rolling Average: ${setpointRollingAverage} - Use Whole Numbers: ${useWholeNumber} - ResetTime: ${spResetTime}<br>"
                app.removeSetting("spResetTime")
                app.updateSetting("useWholeNumber",[value:"false",type:"bool"])
                app.updateSetting("setpointRollingAverage",[value:"false",type:"bool"])
                state.readings = []
            }

            if(accelerationEvent || batteryEvent || contactEvent || humidityEvent || hsmAlertEvent || hsmStatusEvent || illuminanceEvent || modeEvent || motionEvent || powerEvent || presenceEvent || switchEvent || tempEvent || waterEvent) {
                input "setDelay", "bool", defaultValue:false, title: "<b>Set Delay</b>", description: "Delay Time", submitOnChange:true, width:6
                input "randomDelay", "bool", defaultValue:false, title: "<b>Set Random Delay</b>", description: "Random Delay", submitOnChange:true, width:6
                if(setDelay && randomDelay) paragraph "<b>Warning: Please don't select BOTH Set Delay and Random Delay.</b>"
                if(setDelay) {
                    paragraph "Delay the actions until all devices have been in state for XX minutes."
                    input "notifyDelay", "number", title: "Delay (1 to 60)", required:true, multiple:false, range: '1..60', width:6
                    input "minSec", "bool", title: "Use Minutes (off) or Seconds (on)", description: "minSec", defaultValue:false, submitOnChange:true, width:6
                    paragraph "<small>* All devices have to stay in state for the duration of the delay. If any device changes state, the actions will be cancelled.</small>"
                    if(minSec) {
                        minSecValue = "Second(s)"
                    } else {
                        minSecValue = "Minute(s)"
                    }
                    state.theCogTriggers += "<b>-</b> Set Delay: ${setDelay} - notifyDelay: ${notifyDelay} ${minSecValue} - Random Delay: ${randomDelay}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> Set Delay: ${setDelay} - notifyDelay: ${notifyDelay} ${minSecValue} - Random Delay: ${randomDelay}<br>"
                    app.removeSetting("notifyDelay")
                    app.updateSetting("setDelay",[value:"false",type:"bool"])
                }
                if(randomDelay) {
                    paragraph "Delay the actions until all devices have been in state for XX minutes."
                    input "delayLow", "number", title: "Delay Low Limit (1 to 60)", required:true, multiple:false, range: '1..60', submitOnChange:true
                    input "delayHigh", "number", title: "Delay High Limit (1 to 60)", required:true, multiple:false, range: '1..60', submitOnChange:true
                    if(delayHigh <= delayLow) paragraph "<b>Delay High must be greater than Delay Low.</b>"
                    paragraph "<small>* All devices have to stay in state for the duration of the delay. If any device changes state, the notifications will be cancelled.</small>"
                    state.theCogTriggers += "<b>-</b> Random Delay: ${randomDelay} - Delay Low: ${delayLow} - Delay High: ${delayHigh}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> Random Delay: ${randomDelay} - Delay Low: ${delayLow} - Delay High: ${delayHigh}<br>"
                    app.removeSetting("delayLow")
                    app.removeSetting("delayHigh")
                    app.updateSetting("randomDelay",[value:"false",type:"bool"])
                }
            } else {
                app.removeSetting("notifyDelay")
                app.updateSetting("setDelay",[value:"false",type:"bool"])
                app.removeSetting("delayLow")
                app.removeSetting("delayHigh")
                app.updateSetting("randomDelay",[value:"false",type:"bool"])
            }
        }
// ***** Condition Helper Start *****
        section(getFormat("header-green", "${getImage("Blank")}"+" Condition Helper (optional)")) {
            paragraph "This will help the conditions stay true but not trigger the conditions on its own. Examples below!"
            input "useHelper", "bool", title: "Use Condition Helper", defaultValue:false, submitOnChange:true
            if(useHelper) {
                input "myContacts2", "capability.contactSensor", title: "Select the Contact sensor(s) to help keep the conditions true", required:false, multiple:true, submitOnChange:true
                if(myContacts2) input "contactOption2", "enum", title: "Select contact option - If (option), conditions are true", options: ["Open","Closed"], required:true
                input "myMotion2", "capability.motionSensor", title: "Select the Motion sensor(s) to help keep the conditions true", required:false, multiple:true, submitOnChange:true
                if(myMotion2) input "motionOption2", "enum", title: "Select motion option - If (option), conditions are true", options: ["active","inactive"], required:true
                input "myPresence2", "capability.presenceSensor", title: "Select the Presence Sensor(s) to help keep the conditions true", required:false, multiple:true, submitOnChange:true
                if(myPresence2) input "presenceOption2", "enum", title: "Select presence option - If (option), conditions are true", options: ["present","not present"], required:true
                input "mySwitches2", "capability.switch", title: "Select Switch(es) to help keep the conditions true", required:false, multiple:true, submitOnChange:true
                if(mySwitches2) input "switchesOption2", "enum", title: "Select switch option - If (option), conditions are true", options: ["on","off"], required:true
                paragraph "<small>* All helpers are considered 'OR'</small>"
                if(myContacts2) {
                    state.theCogTriggers += "<b>-</b> Condition Helper - Contacts: ${myContacts2} - Option: ${contactOption2}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> Condition Helper - Contacts: ${myContacts2} - Option: ${contactOption2}<br>"
                }
                if(myMotion2) {
                    state.theCogTriggers += "<b>-</b> Condition Helper - Motion: ${myMotion2} - Option: ${motionOption2}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> Condition Helper - Motion: ${myMotion2} - Option: ${motionOption2}<br>"
                }
                if(myPresence2) {
                    state.theCogTriggers += "<b>-</b> Condition Helper - Presence: ${myPresence2} - Option: ${presenceOption2}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> Condition Helper - Presence: ${myPresence2} - Option: ${presenceOption2}<br>"
                }
                if(mySwitches2) {
                    state.theCogTriggers += "<b>-</b> Condition Helper - Switches: ${mySwitches2} - Option: ${switchesOption2}<br>"
                } else {
                    state.theCogTriggers -= "<b>-</b> Condition Helper - Switches: ${mySwitches2} - Option: ${switchesOption2}<br>"
                }
            } else {
                state.theCogTriggers -= "<b>-</b> Condition Helper - Contacts: ${myContacts2} - Option: ${contactOption2}<br>"
                state.theCogTriggers -= "<b>-</b> Condition Helper - Motion: ${myMotion2} - Option: ${motionOption2}<br>"
                state.theCogTriggers -= "<b>-</b> Condition Helper - Presence: ${myPresence2} - Option: ${presenceOption2}<br>"
                state.theCogTriggers -= "<b>-</b> Condition Helper - Switches: ${mySwitches2} - Option: ${switchesOption2}<br>"
                app.removeSetting("myContacts2")
                app.removeSetting("myMotion2")
                app.removeSetting("myPresence2")
                app.removeSetting("mySwitches2")
                app.updateSetting("contactOption2",[value:"false",type:"bool"])
                app.updateSetting("motionOption2",[value:"false",type:"bool"])
                app.updateSetting("presenceOption2",[value:"false",type:"bool"])
                app.updateSetting("switchesOption2",[value:"false",type:"bool"])
            }  
        }
        section("${getImage('instructions')} Condition Helper Examples", hideable: true, hidden: true) {
            paragraph "Examples of Primary and Secondary Condition use"
            paragraph "<b>Bathroom</b><br>Walk into bathroom and trigger the 'Ceiling Motions Sensor' (primary), lights come on. Stay still too long and lights will turn off."
            paragraph "Close the door to trigger the 'contact sensor' (secondary). Even if the motion becomes inactive, (it can't see you when in the shower), the lights will not turn off until that door is opened and the motion is inactive."
            paragraph "<hr>"
            paragraph "<b>Kitchen</b><br>Lights are off - 'Kitchen Ceiling Motion Sensor' (primary) triggers room to be occupied, lights come on.  'Motion sensor under table' (secondary) helps lights to stay on even if 'Kitchen Ceiling Motion Sensor' becomes inactive."
            paragraph "Dog walks under table and triggers the 'Motion sensor under table' (secondary) but the lights were off, lights stay off."
            paragraph "<hr>"
            paragraph "<b>Living Room</b><br>Walk into the room and trigger the 'Ceiling Motion Sensor' (primary), lights come on. If sensor becomes inactive, lights will turn off"
            paragraph "Place phone on 'charger' (secondary). Lights will stay on even if 'Ceiling Motion Sensor' becomes inactive."
            paragraph "<hr>"
            paragraph "<i>Have something neat that you do with primary and secondary triggers? Please post it on the forums and I just might add it here! Thanks</i>"
        }
// ***** Condition Helper End *****        
        // ********** Start Actions **********
        state.theCogActions = "<b><u>Actions</u></b><br>"
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Actions")) {
            input "actionType", "enum", title: "Actions to Perform", options: [
                ["aFan":"Fan Control"],
                ["aGarageDoor":"Garage Doors"],
                ["aHSM":"Hubitat Safety Monitor"],
                ["aLock":"Locks"],
                ["aMode":"Modes"],
                ["aNotification":"Notifications (speech/push/flash)"], 
                ["aRefresh":"Refresh"],
                ["aRule":"Rule Machine"],
                // ["aSendHTTP":"Send HTTP Request"],
                ["aGVar":"Set Global Variable"],
                ["aSwitch":"Switches"],
                ["aSwitchSequence":"Switches In Sequence"],
                ["aSwitchesPerMode":"Switches Per Mode"],
                ["aThermostat":"Thermostat"],
                ["aValve":"Valves"],
                ["aVirtualContact":"* Virtual Contact Sensor"]
            ], required:false, multiple:true, submitOnChange:true
            paragraph "<hr>"
            if(actionType == null) actionType = " "
            
            if(actionType.contains("aFan")) {
                paragraph "<b>Fan Control</b>"
                input "fanAction", "capability.fanControl", title: "Fan Devices", multiple:true, submitOnChange:true
                input "fanSpeed", "enum", title: "Set Fan Speed", required:false, multiple:false, options: ["low","medium-low","medium","medium-high","high","on","off","auto"]
                paragraph "<hr>"
                state.theCogActions += "<b>-</b> Set Fan: ${fanAction} - speed: ${fanSpeed}<br>"
            } else {
                state.theCogActions -= "<b>-</b> Set Fan: ${fanAction} - speed: ${fanSpeed}<br>"
                app.removeSetting("fanAction")
                app.removeSetting("fanSpeed")
            }

            if(actionType.contains("aGarageDoor")) {
                paragraph "<b>Garage Door</b>"
                input "garageDoorClosedAction", "capability.garageDoorControl", title: "Close Devices", multiple:true, submitOnChange:true
                input "garageDoorOpenAction", "capability.garageDoorControl", title: "Open Devices", multiple:true, submitOnChange:true
                paragraph "<hr>"
                state.theCogActions += "<b>-</b> Garage Door - Close Devices: ${garageDoorClosedAction} - Open Devices: ${garageDoorOpenAction}<br>"
            } else {
                state.theCogActions -= "<b>-</b> Garage Door - Close Devices: ${garageDoorClosedAction} - Open Devices: ${garageDoorOpenAction}<br>"
                app.removeSetting("garageDoorClosedAction")
                app.removeSetting("garageDoorOpenAction")
            }

            if(actionType.contains("aHSM")) {
                paragraph "<b>HSM</b>"
                input "setHSM", "enum", title: "Set HSM state", required:false, multiple:false, options: [
                    ["armAway":"Arm Away"],
                    ["armHome":"Arm Home"],
                    ["armNight":"Arm Night"],
                    ["disarm":"Disarm"],
                    ["disarmAll":"Disarm All"],
                    ["armAll":"Arm Monitor Rules"],
                    ["CancelAlerts":"Cancel Alerts"]
                ]
                paragraph "<hr>"
                state.theCogActions += "<b>-</b> Set HSM state: ${setHSM}<br>"
            } else {
                state.theCogActions -= "<b>-</b> Set HSM state: ${setHSM}<br>"
                app.removeSetting("setHSM")
            }

            if(actionType.contains("aLock")) {
                paragraph "<b>Lock</b>"
                input "lockAction", "capability.lock", title: "Lock Devices", multiple:true, submitOnChange:true
                input "unlockAction", "capability.lock", title: "Unlock Devices", multiple:true, submitOnChange:true
                paragraph "<hr>"
                state.theCogActions += "<b>-</b> Lock Devices: ${lockAction} - Unlock Devices: ${unlockAction}<br>"
            } else {
                state.theCogActions -= "<b>-</b> Lock Devices: ${lockAction} - Unlock Devices: ${unlockAction}<br>"
                app.removeSetting("lockAction")
                app.removeSetting("unlockAction")
            }

            if(actionType.contains("aMode")) {
                paragraph "<b>Mode</b>"
                input "modeAction", "mode", title: "Change Mode to", multiple:false, submitOnChange:true
                paragraph "<hr>"
                state.theCogActions += "<b>-</b> Change Mode to: ${modeAction}<br>"
            } else {
                state.theCogActions -= "<b>-</b> Change Mode to: ${modeAction}<br>"
                app.removeSetting("modeAction")
            }

            if(actionType.contains("aNotification")) {
                paragraph "<b>Notification</b>"
                if(useSpeech || sendPushMessage || useTheFlasher) {
                    href "notificationOptions", title:"${getImage("checkMarkGreen")} Notification Options", description:"Click here for options"
                } else {
                    href "notificationOptions", title:"Notification Options", description:"Click here for options"
                }
            } else {
                if(state.theCogNotifications) {
                    state.theCogNotifications -= "<b>-</b> Message when reading is too high: ${messageH}<br>"
                    state.theCogNotifications -= "<b>-</b> Message when reading is too low: ${messageL}<br>"
                    state.theCogNotifications -= "<b>-</b> Message: ${message}<br>"
                    state.theCogNotifications -= "<b>-</b> Use Speech: ${fmSpeaker}<br>"
                    state.theCogNotifications -= "<b>-</b> Send Push: ${sendPushMessage}<br>"
                }
                app.removeSetting("message")
                app.removeSetting("messageH")
                app.removeSetting("messageL")
                app.updateSetting("useSpeech",[value:"false",type:"bool"])
                app.removeSetting("fmSpeaker")
                app.removeSetting("sendPushMessage")
            }

            if(actionType.contains("aRefresh")) {
                paragraph "<b>Refresh Device</b><br><small>* Only works for devices that have the 'refresh' attribute.</small>"
                input "devicesToRefresh", "capability.refresh", title: "Devices to Refresh", multiple:true, submitOnChange:true
                state.theCogActions += "<b>-</b> Devices to Refresh: ${devicesToRefresh}<br>"
            } else {
                state.theCogActions -= "<b>-</b> Devices to Refresh: ${devicesToRefresh}<br>"
                app.removeSetting("devicesToRefresh")
            }

            if(actionType.contains("aRule")) {
                paragraph "<b>Rule Machine</b>"
                def rules = RMUtils.getRuleList()
                if(rules) {
                    input "rmRule", "enum", title: "Select rules", required:false, multiple:true, options: rules, submitOnChange:true
                    if(rmRule) {
                        input "rmAction", "enum", title: "Action", required:false, multiple:false, options: [
                            ["runRuleAct":"Run"],
                            ["stopRuleAct":"Stop"],
                            ["pauseRule":"Pause"],
                            ["resumeRule":"Resume"],
                            ["runRule":"Evaluate"],
                            ["setRuleBooleanTrue":"Set Boolean True"],
                            ["setRuleBooleanFalse":"Set Boolean False"]
                        ]
                    }
                } else {
                    paragraph "No active rules found."
                }
                paragraph "<hr>"
                state.theCogActions += "<b>-</b> Rule: ${rmRule} - Action: ${rmAction}<br>"
            } else {
                state.theCogActions -= "<b>-</b> Rule: ${rmRule} - Action: ${rmAction}<br>"
                app.removeSetting("rmRule")
            }

            if(actionType.contains("aSendHTTP")) {        // Based on code from Dan Ogorchock - @ogiewon - Thank you
                paragraph "<b>Send HTTP Request</b>"
                input "httpIP", "string", title:"Enter IP Address of your HTTP server", submitOnChange:true
                input "httpPort", "string", title:"Enter Port of your HTTP server", defaultValue: "8080", submitOnChange:true
                input "httpPath", "string", title:"The Rest of the URL, Starting with Forward Slash", submitOnChange:true
                input "httpMethod", "enum", title: "Send", options: ["POST","GET","PUT"], defaultValue: "GET", submitOnChange:true, width:6          
                input "httpContent", "enum", title: "Content-Type", options: ["application/x-www-form-urlencoded","application/json"], defaultValue: "application/x-www-form-urlencoded", submitOnChange:true, width:6
                if(httpMethod == "POST" || httpMethod == "PUT") {
                    input "httpBody", "string", title:"Body"
                }
                paragraph "<hr>"
                state.theCogActions += "<b>-</b> Send HTTP: ${httpMethod} - URL: ${httpIP}:${httpPort}${httpPath} - Body: ${httpBody}<br>"
            } else {
                state.theCogActions -= "<b>-</b> Send HTTP: ${httpMethod} - URL: ${httpIP}:${httpPort}${httpPath} - Body: ${httpBody}<br>"
                app.removeSetting("httpURL")
                app.removeSetting("httpMethod")
                app.removeSetting("httpContent")
                app.removeSetting("httpBody")
            }

            if(actionType.contains("aGVar")) {
                paragraph "<b>Set Global Variable</b>"
                paragraph "<small>Be sure to setup a Global Variable in the parent app before trying to use this option.</small>"
                if(state.gvMap) {
                    theList = "${state.gvMap.keySet()}".replace("[","").replace("]","").replace(", ", ",")
                    theList2 = theList.split(",")              
                    input "setGVname", "enum", title: "Select Global Variable to Set", options: theList2, submitOnChange:true, width:6
                    if(setGVname) {
                        input "setGVvalue", "text", title: "Value", required:false, submitOnChange:true, width:6
                    }
                    paragraph "<hr>"
                    state.theCogActions += "<b>-</b> Set Global Variable: ${setGVname} - To: ${setGVvalue}<br>"
                } else {
                    paragraph "<b>In order to use the Global Variables, please be sure to do the following</b><br>- Setup at least one Global Variable in the parent app.<br>- This Cog needs to be saved first. Please scroll down and hit 'Done' before continuing. Then open the Cog again.</b>"
                }
            } else {
                state.theCogActions -= "<b>-</b> Set Global Variable: ${setGVname} - To: ${setGVvalue}<br>"
                app.removeSetting("setGVname")
                app.removeSetting("setGVvalue")
            }
            
            if(actionType.contains("aSwitch")) {
                paragraph "<b>Switch Devices</b>"
                input "switchesOnAction", "capability.switch", title: "Switches to turn On", multiple:true, submitOnChange:true
                input "switchesOffAction", "capability.switch", title: "Switches to turn Off<br><small>Can also be used as Permanent Dim</small>", multiple:true, submitOnChange:true
                if(switchesOnAction) state.theCogActions += "<b>-</b> Switches to turn On: ${switchesOnAction}<br>"
                if(switchesOffAction) state.theCogActions += "<b>-</b> Switches to turn Off: ${switchesOffAction}<br>"
                if(switchesOffAction){
                    input "permanentDim2", "bool", title: "Use Permanent Dim instead of Off", defaultValue:false, submitOnChange:true
                    if(permanentDim2) {
                        paragraph "Instead of turning off, lights will dim to a set level"
                        input "permanentDimLvl2", "number", title: "Permanent Dim Level (1 to 99)", range: '1..99'
                        input "pdColorTemp2", "bool", title: "Use Color (off) or Temperature (on)", defaultValue:false, submitOnChange:true
                        if(pdColorTemp2) {
                            input "pdTemp2", "number", title: "Color Temperature", submitOnChange:true
                            app.removeSetting("pdColor2")
                        } else {
                            input "pdColor2", "enum", title: "Color (leave blank for no change)", required:false, multiple:false, options: [
                                ["Soft White":"Soft White - Default"],
                                ["White":"White - Concentrate"],
                                ["Daylight":"Daylight - Energize"],
                                ["Warm White":"Warm White - Relax"],
                                "Red","Green","Blue","Yellow","Orange","Purple","Pink"], submitOnChange:true
                            app.removeSetting("pdTemp2")
                        }
                    } else {
                        app.removeSetting("permanentDimLvl2")
                        app.updateSetting("pdColorTemp2",[value:"false",type:"bool"])
                        app.removeSetting("pdColor2")
                        app.removeSetting("pdTemp2")
                    }
                    if(permanentDim2) state.theCogActions += "<b>-</b> Use Permanent Dim instead of Off: ${permanentDim2} - Level: ${permanentDimLvl2} - color: ${pdColor2} - Temp: ${pdTemp2}<br>"
                } else {
                    state.theCogActions -= "<b>-</b> Use Permanent Dim instead of Off: ${permanentDim2} - Level: ${permanentDimLvl2} - color: ${pdColor2} - Temp: ${pdTemp2}<br>"
                    app.removeSetting("permanentDimLvl2")
                    app.removeSetting("pdColor2")
                    app.removeSetting("pdTemp2")
                    app.updateSetting("permanentDim2",[value:"false",type:"bool"])
                }
                
                input "switchesToggleAction", "capability.switch", title: "Switches to Toggle", multiple:true, submitOnChange:true
                if(switchesToggleAction) {
                    state.theCogActions += "<b>-</b> Switches to Toggle: ${switchesToggleAction}<br>"
                } else {
                    state.theCogActions -= "<b>-</b> Switches to Toggle: ${switchesToggleAction}<br>"
                }
                paragraph "<hr>"
                input "setOnLC", "capability.switchLevel", title: "Dimmer to set", required:false, multiple:true, submitOnChange:true
                if(setOnLC) {
                    input "levelLC", "number", title: "On Level (1 to 99)", required:false, multiple:false, defaultValue: 99, range: '1..99'
                    input "lcColorTemp", "bool", title: "Use Color (off) or Temperature (on)", defaultValue:false, submitOnChange:true
                    if(lcColorTemp) {
                        input "tempLC", "number", title: "Color Temperature", submitOnChange:true
                        app.removeSetting("colorLC")
                    } else {
                        input "colorLC", "enum", title: "Color (leave blank for no change)", required:false, multiple:false, options: [
                            ["Soft White":"Soft White - Default"],
                            ["White":"White - Concentrate"],
                            ["Daylight":"Daylight - Energize"],
                            ["Warm White":"Warm White - Relax"],
                            "Red","Green","Blue","Yellow","Orange","Purple","Pink"], submitOnChange:true
                        app.removeSetting("tempLC")
                    }
                    state.theCogActions += "<b>-</b> Dimmers to Set: ${setOnLC} - On Level: ${levelLC} - Color: ${colorLC} - Temp: ${tempLC}<br>"   
                } else {
                    state.theCogActions -= "<b>-</b> Switches to Toggle: ${switchesToggleAction}<br>"
                    state.theCogActions -= "<b>-</b> Dimmers to Set: ${setOnLC} - On Level: ${levelLC} - Color: ${colorLC} - Temp: ${tempLC}<br>"
                    app.removeSetting("setOnLC")
                    app.removeSetting("levelLC")
                    app.removeSetting("colorLC")
                }
                input "switchedDimUpAction", "bool", defaultValue:false, title: "Slowly Dim Lighting UP", description: "Dim Up", submitOnChange:true, width:6
                input "switchedDimDnAction", "bool", defaultValue:false, title: "Slowly Dim Lighting DOWN", description: "Dim Down", submitOnChange:true, width:6

                if(switchedDimUpAction) {
                    paragraph "<hr>"
                    paragraph "<b>Slowly Dim Lighting UP</b>"
                    input "slowDimmerUp", "capability.switchLevel", title: "Select dimmer devices to slowly rise", required:true, multiple:true
                    input "minutesUp", "number", title: "Takes how many minutes to raise (1 to 60)", required:true, multiple:false, defaultValue:15, range: '1..60'
                    input "startLevelHigh", "number", title: "Starting Level (5 to 99)", required:true, multiple:false, defaultValue: 5, range: '5..99'
                    input "targetLevelHigh", "number", title: "Target Level (5 to 99)", required:true, multiple:false, defaultValue: 99, range: '5..99'
                    input "colorUp", "enum", title: "Color", required:true, multiple:false, options: [
                        ["Soft White":"Soft White - Default"],
                        ["White":"White - Concentrate"],
                        ["Daylight":"Daylight - Energize"],
                        ["Warm White":"Warm White - Relax"],
                        "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
                    paragraph "Slowly raising a light level is a great way to wake up in the morning. If you want everything to delay happening until the light reaches its target level, turn this switch on."
                    input "targetDelay", "bool", defaultValue:false, title: "<b>Delay Until Finished</b>", description: "Target Delay", submitOnChange:true
                    state.theCogActions += "<b>-</b> Select dimmer devices to slowly rise: ${slowDimmerUp} - Minutes: ${minutesUp} - Starting Level: ${startLevelHigh} - Target Level: ${targetLevelHigh} - Color: ${colorUp}<br>"
                } else {
                    state.theCogActions -= "<b>-</b> Select dimmer devices to slowly rise: ${slowDimmerUp} - Minutes: ${minutesUp} - Starting Level: ${startLevelHigh} - Target Level: ${targetLevelHigh} - Color: ${colorUp}<br>"
                    app.removeSetting("slowDimmerUp")
                    app.removeSetting("minutesUp")
                    app.removeSetting("startLevelHigh")
                    app.removeSetting("targetLevelHigh")
                    app.removeSetting("colorUp")
                    app.updateSetting("targetDelay",[value:"false",type:"bool"])
                }

                if(switchedDimDnAction) {
                    paragraph "<hr>"
                    paragraph "<b>Slowly Dim Lighting DOWN</b>"
                    input "slowDimmerDn", "capability.switchLevel", title: "Select dimmer devices to slowly dim", required:true, multiple:true
                    input "minutesDn", "number", title: "Takes how many minutes to dim (1 to 60)", required:true, multiple:false, defaultValue:15, range: '1..60'
                    input "useMaxLevel", "bool", title: "Use a set starting level for all lights (off) or dim from the current level of each light (on)", defaultValue:false, submitOnChange:true
                    if(useMaxLevel) {
                        paragraph "The highest level light will start the process of dimming, each light will join in as the dim level reaches the lights current value"
                        app.removeSetting("startLevelLow")
                    } else {
                        input "startLevelLow", "number", title: "Starting Level (5 to 99)", required:true, multiple:false, defaultValue: 99, range: '5..99'
                    }

                    input "targetLevelLow", "number", title: "Target Level (5 to 99)", required:true, multiple:false, defaultValue: 5, range: '5..99'
                    input "dimDnOff", "bool", defaultValue:false, title: "<b>Turn dimmer off after target is reached</b>", description: "Dim Off Options", submitOnChange:true
                    input "colorDn", "enum", title: "Color", required:true, multiple:false, options: [
                        ["Soft White":"Soft White - Default"],
                        ["White":"White - Concentrate"],
                        ["Daylight":"Daylight - Energize"],
                        ["Warm White":"Warm White - Relax"],
                        "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
                    state.theCogActions += "<b>-</b> Select dimmer devices to slowly dim: ${slowDimmerUp} - Minutes: ${minutesDn} - useMaxLevel: ${useMaxLevel} - Starting Level: ${startLevelLow} - Target Level: ${targetLevelLow} - Dim to Off: ${dimDnOff} - Color: ${colorDn}<br>"
                } else {
                    state.theCogActions -= "<b>-</b> Select dimmer devices to slowly dim: ${slowDimmerUp} - Minutes: ${minutesDn} - useMaxLevel: ${useMaxLevel} - Starting Level: ${startLevelLow} - Target Level: ${targetLevelLow} - Dim to Off: ${dimDnOff} - Color: ${colorDn}<br>"
                    app.removeSetting("slowDimmerDn")
                    app.removeSetting("minutesDn")
                    app.removeSetting("startLevelLow")
                    app.removeSetting("targetLevelLow")
                    app.removeSetting("dimDnOff")
                    app.removeSetting("colorDn")
                    app.updateSetting("useMaxLevel",[value:"false",type:"bool"])
                    app.updateSetting("dimDnOff",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                state.theCogActions -= "<b>-</b> Switches to turn On: ${switchesOnAction}<br>"
                state.theCogActions -= "<b>-</b> Switches to turn Off: ${switchesOffAction}<br>"
                app.removeSetting("switchesOnAction")
                app.removeSetting("switchesOffAction")
                app.removeSetting("switchesToggleAction")
                app.updateSetting("switchedDimUpAction",[value:"false",type:"bool"])
                app.updateSetting("switchedDimDnAction",[value:"false",type:"bool"])
                app.updateSetting("lcColorTemp",[value:"false",type:"bool"])
            }
                         
            if(actionType.contains("aSwitchSequence")) {
                paragraph "<b>Switches In Sequence</b>"
                paragraph "Sometimes you need things to turn on in a specific order. This section will do just that. Great for entertainment systems!"
                input "deviceSeqAction1", "capability.switch", title: "Switches to turn On - 1", multiple:true, submitOnChange:true
                input "deviceSeqAction2", "capability.switch", title: "Switches to turn On - 2", multiple:true, submitOnChange:true
                input "deviceSeqAction3", "capability.switch", title: "Switches to turn On - 3", multiple:true, submitOnChange:true
                input "deviceSeqAction4", "capability.switch", title: "Switches to turn On - 4", multiple:true, submitOnChange:true
                input "deviceSeqAction5", "capability.switch", title: "Switches to turn On - 5", multiple:true, submitOnChange:true             
                state.theCogActions += "<b>-</b> Switches to turn On in order: ${deviceSeqAction1} - ${deviceSeqAction2} - ${deviceSeqAction3} - ${deviceSeqAction4} - ${deviceSeqAction5}<br>"
                paragraph "<small>* Note: If Reverse Action is selected below, the switches selected here will turn off in reverse order. ie. 5,4,3,2,1</small>"
                paragraph "<hr>"
            } else {
                state.theCogActions -= "<b>-</b> Switches to turn On in order: ${deviceSeqAction1} - ${deviceSeqAction2} - ${deviceSeqAction3} - ${deviceSeqAction4} - ${deviceSeqAction5}<br>"
                app.removeSetting("deviceSeqAction1")
                app.removeSetting("deviceSeqAction2")
                app.removeSetting("deviceSeqAction3")
                app.removeSetting("deviceSeqAction4")
                app.removeSetting("deviceSeqAction5")
            }

// ***** Start Switches per Mode *****   
            if(actionType.contains("aSwitchesPerMode")) {
                paragraph "<b>Switches Per Mode</b>"
                input "setDimmersPerMode", "capability.switchLevel", title: "Dimmers to set Per Mode", required:false, multiple:true, submitOnChange:true
                if(setDimmersPerMode) {                    
                    paragraph "- <b>To add or edit Mode Lighting</b>, simply fill in the Name and Value below. Values will be added as soon as you click outside the value field.<br>- <b>To delete a variable</b>, enter in the Name and flip the Add/Edit Delete switch to on."
                    input "sdPerModeName", "mode", title: "Mode", required:false, width:6
                    input "sdPerModeLevel", "number", title: "On Level (1 to 99)", required:false, multiple:false, defaultValue: 99, range: '1..99'
                    input "sdPerModeColorTemp", "bool", title: "Use Color (off) or Temperature (on)", defaultValue:false, submitOnChange:true
                    if(sdPerModeColorTemp) {
                        input "sdPerModeTemp", "number", title: "Color Temperature", submitOnChange:true
                        app.removeSetting("sdPerModeColor")
                    } else {
                        input "sdPerModeColor", "enum", title: "Color (leave blank for no change)", required:false, multiple:false, options: [
                            ["Soft White":"Soft White - Default"],
                            ["White":"White - Concentrate"],
                            ["Daylight":"Daylight - Energize"],
                            ["Warm White":"Warm White - Relax"],
                            "Red","Green","Blue","Yellow","Orange","Purple","Pink"], submitOnChange:true
                        app.removeSetting("sdPerModeTemp")
                    }
                    input "sdPerModeAdd", "button", title: "Add/Edit Mode", width: 3
                    input "sdPerModeDel", "button", title: "Delete Mode", width: 3
                    input "sdPerModeRefresh", "button", title: "Refresh Table", width: 3
                    input "sdPerModeClear", "button", title: "Clear Table", width: 3
                    paragraph "<small>* Each action will happen immediately after pressing button.</small>"
                    paragraph "<hr>"
                    if(state.thePerModeMap == null) {
                        theMap = "No devices are setup"
                    } else {
                        theMap = state.thePerModeMap
                    }
                    paragraph "${theMap}"
                    paragraph "<hr>"
                    
                    state.theCogActions += "<b>-</b> Switches Per Mode: ${setDimmersPerMode}<br>${state.thePerModeMap}<br>"   
                } else {
                    state.theCogActions -= "<b>-</b> Switches Per Mode: ${setDimmersPerMode}<br>${state.thePerModeMap}<br>"
                    app.removeSetting("setDimmersPerMode")
                    app.removeSetting("sdPerModeName")
                    app.removeSetting("sdPerModeLevel")
                    app.removeSetting("sdPerModeTemp")
                    app.removeSetting("sdPerModeColor")
                    state.sdPerModeMap = [:]
                    state.thePerModeMap = null
                }
            } else {
                state.theCogActions -= "<b>-</b> Switches Per Mode: ${setDimmersPerMode}<br>${state.thePerModeMap}<br>"
                app.removeSetting("setDimmersPerMode")
                app.removeSetting("sdPerModeName")
                app.removeSetting("sdPerModeLevel")
                app.removeSetting("sdPerModeTemp")
                app.removeSetting("sdPerModeColor")
                state.sdPerModeMap = [:]
                state.thePerModeMap = null
            }
// ***** End SwitchLevel per Mode *****                 

            if(actionType.contains("aThermostat")) {
                paragraph "<b>Thermostat</b>"
                input "thermostatAction", "capability.thermostat", title: "Thermostats", multiple:true, submitOnChange:true
                if(thermostatAction) {
                    input "setThermostatFanMode", "enum", title: "Set Thermostat Fan mode", required:false, multiple:false, options: ["on", "circulate", "auto"], submitOnChange:true
                    input "setThermostatMode", "enum", title: "Set Thermostat mode", required:false, multiple:false, options: ["auto", "off", "heat", "emergency heat", "cool"], submitOnChange:true
                    input "coolingSetpoint", "number", title: "Set Cooling Setpoint", required:false, multiple:false, submitOnChange:true, width:6
                    input "heatingSetpoint", "number", title: "Set Heating Setpoint", required:false, multiple:false, submitOnChange:true, width:6
                    
                }
                paragraph "<hr>"
                if(setThermostatMode) state.theCogActions += "<b>-</b> Set Thermostats (${thermostatAction}) to mode: ${setThermostatMode}<br>"
                if(coolingSetpoint) state.theCogActions += "<b>-</b> Set Thermostats Cooling Setpoint to: ${coolingSetpoint}<br>"
                if(heatingSetpoint) state.theCogActions += "<b>-</b> Set Thermostats Heating Setpoint to: ${heatingSetpoint}<br>"
            } else {
                state.theCogActions -= "<b>-</b> Set Thermostats ${thermostatAction} to mode: ${setThermostatMode}<br>"
                state.theCogActions -= "<b>-</b> Set Thermostats Cooling Setpoint to: ${coolingSetpoint}<br>"
                state.theCogActions -= "<b>-</b> Set Thermostats Heating Setpoint to: ${heatingSetpoint}<br>"               
                app.removeSetting("thermostatAction")
                app.removeSetting("setThermostatMode")
            }
            
            if(actionType.contains("aValve")) {
                paragraph "<b>Valves</b>"
                input "valveClosedAction", "capability.valve", title: "Close Devices", multiple:true, submitOnChange:true
                input "valveOpenAction", "capability.valve", title: "Open Devices", multiple:true, submitOnChange:true
                paragraph "<hr>"
                state.theCogActions += "<b>-</b> Close Valves: ${valveClosedAction} - Open Valves: ${valveOpenAction}<br>"
            } else {
                state.theCogActions -= "<b>-</b> Close Valves: ${valveClosedAction} - Open Valves: ${valveOpenAction}<br>"
                app.removeSetting("valveClosedAction")
                app.removeSetting("valveOpenAction")
            }
            
            if(actionType.contains("aVirtualContact")) {
                paragraph "<b>Virtual Contact Sensor</b><br><small>* Can be used with Alexa Routines!</small>"
                input "contactCloseAction", "capability.contactSensor", title: "Close Sensors", multiple:true, submitOnChange:true
                input "contactOpenAction", "capability.contactSensor", title: "Open Sensors", multiple:true, submitOnChange:true
                paragraph "<hr>"
                state.theCogActions += "<b>-</b> Virtual Contact Sensor - Close Sensors: ${contactClosedAction} - Open Sensors: ${contactOpenAction}<br>"
            } else {
                state.theCogActions -= "<b>-</b> Virtual Contact Sensor - Close Sensors: ${contactClosedAction} - Open Sensors: ${contactOpenAction}<br>"
                app.removeSetting("contactClosedAction")
                app.removeSetting("contactOpenAction")
            }      
        
            // Reverse Options
            if(fanAction || switchesOnAction || switchesOffAction || deviceSeqAction1 || setOnLC || contactOpenAction || setDimmersPerMode) {
                if(batteryEvent || humidityEvent || illuminanceEvent || powerEvent || tempEvent || (customEvent && deviceORsetpoint)) {
                    paragraph "<b><small>Please choose only ONE of the following:</b></small>"
                    input "reverseWhenHigh", "bool", title: "Reverse actions when conditions are no longer true - Setpoint is High", defaultValue:false, submitOnChange:true
                    input "reverseWhenLow", "bool", title: "Reverse actions when conditions are no longer true - Setpoint is Low", defaultValue:false, submitOnChange:true
                    input "reverseWhenBetween", "bool", title: "Reverse actions when conditions are no longer true - Setpoint is Between", defaultValue:false, submitOnChange:true
                    app.updateSetting("reverse",[value:"false",type:"bool"])
                } else {
                    input "reverse", "bool", title: "Reverse actions when conditions are no longer true (immediately)", defaultValue:false, submitOnChange:true
                    input "reverseWithDelay", "bool", title: "Reverse actions when conditions are no longer true (with delay)", defaultValue:false, submitOnChange:true
                    if(reverseWithDelay) {
                        paragraph "<hr>"
                        input "dimWhileDelayed", "bool", title: "Dim lights during delay as a warning", defaultValue:false, submitOnChange:true, width:11
                        if(dimWhileDelayed) { 
                            input "warningDimLvl", "number", title: "Warning Dim Level (1 to 99)", range: '1..99'
                            paragraph "<small>* This level will override the Permanent Dim option below.</small>"
                        }
                        paragraph "<hr>"
                    } else {
                        app.removeSetting("warningDimLvl")
                    }
                    input "timeReverse", "bool", title: "Reverse actions after a set number of minutes (even if Conditions are still true)", defaultValue:false, submitOnChange:true
                    if(reverseWithDelay || timeReverse) {
                        input "timeToReverse", "number", title: "Time to Reverse (in minutes)", submitOnChange:true
                    }
                    paragraph "<small><b>* Please only select ONE Reverse option.</b></small>"
                    app.updateSetting("reverseWhenHigh",[value:"false",type:"bool"])
                    app.updateSetting("reverseWhenLow",[value:"false",type:"bool"])
                    app.updateSetting("reverseWhenBetween",[value:"false",type:"bool"])
                    if(!reverseWithDelay && !timeReverse) {
                        app.removeSetting("timeToReverse")
                    }
                }
                if(reverse || reverseWhenHigh || reverseWhenLow || reverseWhenBetween || timeReverse || reverseWithDelay) {
                    if(reverse) {
                        state.theCogActions += "<b>-</b> Reverse: ${reverse}<br>"
                    } else if(reverseWhenHigh || reverseWhenLow || reverseWhenBetween) {
                        state.theCogActions += "<b>-</b> Reverse High: ${reverseWhenHigh} - Reverse Low: ${reverseWhenLow} - Reverse Between: ${reverseWhenBetween}<br>"
                    } else if(timeReverse) {
                        state.theCogActions += "<b>-</b> Reverse: ${timeToReverse} minute(s), even if Conditions are still true<br>"
                    } else if(reverseWithDelay) {
                        state.theCogActions += "<b>-</b> Reverse: ${timeToReverse} minute(s), after Conditions become false - Dim While Delayed: ${dimWhileDelayed}<br>"
                    }
                } else {
                    state.theCogActions -= "<b>-</b> Reverse: ${reverse}<br>"
                    state.theCogActions -= "<b>-</b> Reverse High: ${reverseWhenHigh} - Reverse Low: ${reverseWhenLow} - Reverse Between: ${reverseWhenBetween}<br>"
                    state.theCogActions -= "<b>-</b> Reverse: ${timeToReverse} minute(s), even if Conditions are still true<br>"
                    state.theCogActions -= "<b>-</b> Reverse: ${timeToReverse} minute(s), after Conditions become false - Dim While Delayed: ${dimWhileDelayed}<br>"
                }
                if((reverse || reverseWithDelay || reverseWhenHigh || reverseWhenLow || reverseWhenBetween) && (switchesOnAction || setOnLC || setDimmersPerMode)){
                    paragraph "<hr>"
                    paragraph "If a light has been turned on, Reversing it will turn it off. But with the Permanent Dim option, the light can be Dimmed to a set level and/or color instead!"
                    input "permanentDim", "bool", title: "Use Permanent Dim instead of Off", defaultValue:false, submitOnChange:true
                    if(permanentDim) {
                        paragraph "Instead of turning off, lights will dim to a set level"
                        input "permanentDimLvl", "number", title: "Permanent Dim Level (1 to 99)", range: '1..99'
                        input "pdColorTemp", "bool", title: "Use Color (off) or Temperature (on)", defaultValue:false, submitOnChange:true
                        if(pdColorTemp) {
                            input "pdTemp", "number", title: "Color Temperature", submitOnChange:true
                            app.removeSetting("pdColor")
                        } else {
                            input "pdColor", "enum", title: "Color (leave blank for no change)", required:false, multiple:false, options: [
                                ["Soft White":"Soft White - Default"],
                                ["White":"White - Concentrate"],
                                ["Daylight":"Daylight - Energize"],
                                ["Warm White":"Warm White - Relax"],
                                "Red","Green","Blue","Yellow","Orange","Purple","Pink"], submitOnChange:true
                            app.removeSetting("pdTemp")
                        }
                    } else {
                        app.removeSetting("permanentDimLvl")
                        app.updateSetting("pdColorTemp",[value:"false",type:"bool"])
                        app.removeSetting("pdColor")
                        app.removeSetting("pdTemp")
                    }
                    if(permanentDim) state.theCogActions += "<b>-</b> Use Permanent Dim: ${permanentDim} - PD Level: ${permanentDimLvl} - PD Color: ${pdColor} - Temp: ${pdTemp}<br>"
                } else {
                    state.theCogActions -= "<b>-</b> Use Permanent Dim: ${permanentDim} - Permanent Dim Level: ${permanentDimLvl} - PD Color: ${pdColor} - Temp: ${pdTemp}<br>"
                    app.removeSetting("permanentDimLvl")
                    app.removeSetting("pdColor")
                    app.updateSetting("permanentDim",[value:"false",type:"bool"])
                    app.updateSetting("pdColorTemp",[value:"false",type:"bool"])
                }
                paragraph "<hr>"
            } else {
                app.removeSetting("timeToReverse")
                app.updateSetting("timeReverse",[value:"false",type:"bool"])
                app.removeSetting("permanentDimLvl")
                app.removeSetting("pdColor")
                app.updateSetting("pdColorTemp",[value:"false",type:"bool"])
                app.updateSetting("permanentDim",[value:"false",type:"bool"])
                app.updateSetting("reverse",[value:"false",type:"bool"])
                app.updateSetting("reverseWithDelay",[value:"false",type:"bool"])
                app.removeSetting("warningDimLvl")
            }        
            paragraph "<b>Special Action Option</b><br>Sometimes devices can miss commands due to HE's speed. This option will allow you to adjust the time between commands being sent."
            input "actionDelay", "number", title: "Delay (in milliseconds - 1000 = 1 second, 3 sec max)", range: '1..3000', defaultValue:100, required:true, submitOnChange:true
            state.theCogActions += "<b>-</b> Delay: ${actionDelay}<br>"
            if(actionDelay == null || actionDelay == "") {
                state.theCogActions -= "<b>-</b> Delay: ${actionDelay}<br>"
                app.updateSetting("actionDelay",[value:"100",type:"number"])
            }
        }                
        // ********** End Actions **********

        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains("(Paused)")) {
                        app.updateLabel(app.label + " <span style='color:red'>(Paused)</span>")
                    }
                }
            } else {
                if(app.label) {
                    if(app.label.contains("(Paused)")) {
                        app.updateLabel(app.label - " <span style='color:red'>(Paused)</span>")
                    }
                }
            }
        }
        section() {
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            if(pauseApp) { 
                paragraph app.label
            } else {
                label title: "Enter a name for this automation", required:false
            }
            input "runNow", "bool", title: "Run Cog when Saving", description: "Run Now", defaultValue:false, submitOnChange:true
            input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
            if(logEnable) {
                input "logSize", "bool", title: "Use Short Logs (off) or Long Logs (On) - Please only post long logs if the Developer asks for it", description: "log size", defaultValue:false, submitOnChange:true
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours"]
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" The Cog Description")) {
            paragraph "This will give a short description on how the Cog will operate. This is also an easy way to share how to do things. Just copy the text below and post it on the HE forums!"
            paragraph "<hr>"
            paragraph "<b>Event Engine Cog - ver. ${state.version}</b>"
            if(state.theCogTriggers) paragraph state.theCogTriggers.replaceAll("null","NA")
            if(state.theCogActions) paragraph state.theCogActions.replaceAll("null","NA")
            if(state.theCogNotifications) paragraph state.theCogNotifications.replaceAll("null","NA")
            paragraph "<hr>"
            paragraph "<small>* If you're not seeing your Notification settings, please re-visit the Notifications section.</small>"
            input "resetCog", "bool", defaultValue:false, title: "Refresh The Cog Description <small>(This will happen immediately)</small>", description: "Cog", submitOnChange:true
            if(resetCog) {
                app.updateSetting("resetCog",[value:"false",type:"bool"])
            }
        }
        display2()
    }
}

def notificationOptions(){
    dynamicPage(name: "notificationOptions", title: "Notification Options", install:false, uninstall:false){
        state.theCogNotifications = "<b><u>Notifications</u></b><br>"
        section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications. Please be sure to have Follow Me installed before trying to send any notifications."
            input "useSpeech", "bool", title: "Use Speech through Follow Me", defaultValue:false, submitOnChange:true
            if(useSpeech) {
                input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required:true, submitOnChange:true
                state.theCogNotifications += "<b>-</b> Use Speech: ${fmSpeaker}<br>"
            } else {
                state.theCogNotifications -= "<b>-</b> Use Speech: ${fmSpeaker}<br>"
                app.removeSetting("fmSpeaker")
            }
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification", multiple:true, required:false, submitOnChange:true
            if(sendPushMessage) {
                state.theCogNotifications += "<b>-</b> Send Push: ${sendPushMessage}<br>"
            } else {
                state.theCogNotifications -= "<b>-</b> Send Push: ${sendPushMessage}<br>"
            }
        }

        if(useSpeech || sendPushMessage) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Priority Message Instructions")) { }
            section("Instructions for Priority Message:", hideable:true, hidden:true) {
                paragraph "Message Priority is a unique feature only found with 'Follow Me'! Simply place the option bracket in front of any message to be spoken and the Volume, Voice and/or Speaker will be adjusted accordingly."
                paragraph "Format: [priority:sound:speaker]<br><small>Note: Any option not needed, replace with a 0 (zero).</small>"
                paragraph "<b>Priority:</b><br>This can change the voice used and the color of the message displayed on the Dashboard Cog.<br>[F:0:0] - Fun<br>[R:0:0] - Random<br>[L:0:0] - Low<br>[N:0:0] - Normal<br>[H:0:0] - High"
                paragraph "<b>Sound:</b><br>You can also specify a sound file to be played before a message!<br>[1] - [5] - Specify a files URL"
                paragraph "<b>ie.</b> [L:0:0]Amy is home or [N:3:0]Window has been open too long or [H:0:0]Heat is on and window is open"
                paragraph "If you JUST want a sound file played with NO speech after, use [L:1:0]. or [N:3:0]. etc. Notice the DOT after the [], that is the message and will not be spoken."
                paragraph "<b>Speaker:</b><br>While Follow Me allows you to setup your speakers in many ways, sometimes you want it to ONLY speak on a specific device. This option will do just that! Just replace with the corresponding speaker number from the Follow Me Parent App."
                paragraph "<b>*</b> <i>Be sure to have the 'Priority Speaker Options' section completed in the Follow Me Parent App.</i>"
                paragraph "<hr>"
                paragraph "<b>General Notes:</b>"
                paragraph "Priority Voice and Sound options are only available when using Speech Synth option.<br>Also notice there is no spaces between the option and the message."
                paragraph "<b>ie.</b> [N:3:0]Window has been open too long"
            }

            section(getFormat("header-green", "${getImage("Blank")}"+" Messages Options")) {
                wc =  "%whoHappened% - Device that caused the event to trigger<br>"
                wc += "%whatHappened% - Device status that caused the event to trigger<br>"
                wc += "%time% - Will speak the current time in 24 h<br>"
                wc += "%time1% - Will speak the current time in 12 h<br>"
                if(lockEvent) wc += "%whoUnlocked% - The name of the person who unlocked the door<br>"
                paragraph wc

                if(triggerType.contains("xBattery") || triggerType.contains("xEnergy") || triggerType.contains("xHumidity") || triggerType.contains("xIlluminance") || triggerType.contains("xPower") || triggerType.contains("xTemp")) {
                    paragraph "<b>Setpoint Message Options</b>"
                    input "messageH", "text", title: "Message to speak when reading is too high", required:false, submitOnChange:true
                    input "messageL", "text", title: "Message to speak when reading is too low", required:false, submitOnChange:true
                    if(messageH) state.theCogNotifications += "<b>-</b> Message when reading is too high: ${messageH}<br>"
                    if(messageL) state.theCogNotifications += "<b>-</b> Message when reading is too low: ${messageL}<br>"
                } else {
                    state.theCogNotifications -= "<b>-</b> Message when reading is too high: ${messageH}<br>"
                    state.theCogNotifications -= "<b>-</b> Message when reading is too low: ${messageL}<br>"
                    app.removeSetting("messageH")
                    app.removeSetting("messageL")
                }

                if(!triggerType.contains("xBattery") || !triggerType.contains("xEnergy") || !triggerType.contains("xHumidity") && !triggerType.contains("xIlluminance") && !triggerType.contains("xPower") && !triggerType.contains("xTemp")) {
                    paragraph "<b>Random Message Options</b>"
                    input "message", "text", title: "Message to be spoken/pushed - Separate each message with <b>;</b> (semicolon)", required:false, submitOnChange:true
                    input "msgList", "bool", defaultValue:false, title: "Show a list view of the messages", description: "List View", submitOnChange:true
                    if(message) state.theCogNotifications += "<b>-</b> Message: ${message}<br>"
                    if(msgList) {
                        def values = "${message}".split(";")
                        listMap = ""
                        values.each { item -> listMap += "${item}<br>"}
                        paragraph "${listMap}"
                    }
                } else {
                    state.theCogNotifications -= "<b>-</b> Message: ${message}<br>"
                    app.removeSetting("message")
                }
            }
                
            section(getFormat("header-green", "${getImage("Blank")}"+" Repeat Notifications")) {
                input "msgRepeat", "bool", title: "Repeat Notifications", description: "List View", defaultValue:false, submitOnChange:true
                if(msgRepeat) {
                    input "msgRepeatMinutes", "number", title: "Repeat every XX minutes", submitOnChange:true, width:6
                    input "msgRepeatMax", "number", title: "Max number of repeats", submitOnChange:true, width:6
                    
                    if(msgRepeatMinutes && msgRepeatMax) {
                    paragraph "Message will repeat every ${msgRepeatMinutes} minutes until one of the contacts/switches changes state <b>OR</b> the Max number of repeats is reached (${msgRepeatMax})"
                        repeatTimeSeconds = ((msgRepeatMinutes * 60) * msgRepeatMax)
                        int inputNow=repeatTimeSeconds
                        int nDayNow = inputNow / 86400
                        int nHrsNow = (inputNow % 86400 ) / 3600
                        int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
                        int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
                        paragraph "In this case, it would take ${nHrsNow} Hours, ${nMinNow} Mins and ${nSecNow} Seconds to reach the max number of repeats (if nothing changes state)"
                    }
                    
                    input "msgRepeatContact", "capability.contactSensor", title: "Contact to turn the Repeat Off", multiple:false, submitOnChange:true
                    input "msgRepeatSwitch", "capability.switch", title: "Switch to turn the Repeat Off", multiple:false, submitOnChange:true 
                    if(msgRepeatContact) { paragraph "<small>* Contact will turn off Repeat when changing to any state.</small>" }
                    if(msgRepeatSwitch) { paragraph "<small>* Switch will turn off Repeat when changing to any state.</small>" }
                    state.theCogNotifications += "<b>-</b> msgRepeat: ${msgRepeat} - msgRepeatMinutes: ${msgRepeatMinutes} - msgRepeatContact: ${msgRepeatContact} - msgRepeatSwitch: ${msgRepeatSwitch}<br>"
                } else {
                    state.theCogNotifications -= "<b>-</b> msgRepeat: ${msgRepeat} - msgRepeatMinutes: ${msgRepeatMinutes} - msgRepeatContact: ${msgRepeatContact} - msgRepeatSwitch: ${msgRepeatSwitch}<br>"
                    app.removeSetting("msgRepeatMinutes")
                    app.removeSetting("msgRepeatContact")
                    app.removeSetting("msgRepeatSwitch")
                    app.removeSetting("msgRepeatMax")
                }
            }
        } else {
            state.theCogNotifications -= "<b>-</b> Message when reading is too high: ${messageH}<br>"
            state.theCogNotifications -= "<b>-</b> Message when reading is too low: ${messageL}<br>"
            state.theCogNotifications -= "<b>-</b> Message: ${message}<br>"
            state.theCogNotifications -= "<b>-</b> Use Speech: ${fmSpeaker}<br>"
            state.theCogNotifications -= "<b>-</b> Send Push: ${sendPushMessage}<br>"
            app.removeSetting("message")
            app.removeSetting("messageH")
            app.removeSetting("messageL")
            app.updateSetting("useSpeech",[value:"false",type:"bool"])
            app.removeSetting("fmSpeaker")
            app.removeSetting("sendPushMessage")
            app.updateSetting("msgRepeat",[value:"false",type:"bool"])
            app.removeSetting("msgRepeatMinutes")
            app.removeSetting("msgRepeatContact")
            app.removeSetting("msgRepeatSwitch")
            app.removeSetting("msgRepeatMax")
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Flash Lights Options")) {
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-the-flasher-flash-your-lights-based-on-several-triggers/30843' target=_blank>The Flasher</a> to process Flashing Lights. Please be sure to have The Flasher installed before trying to use this option."
            input "useTheFlasher", "bool", title: "Use The Flasher", defaultValue:false, submitOnChange:true
            if(useTheFlasher) {
                input "theFlasherDevice", "capability.actuator", title: "The Flasher Device containing the Presets you wish to use", required:true, multiple:false
                input "flashOnTriggerPreset", "number", title: "Select the Preset to use when Notifications are triggered (1..5)", required:true, submitOnChange:true
                if(useTheFlasher) state.theCogNotifications += "<b>-</b> Use The Flasher: ${useTheFlasher} - Device: ${theFlasherDevice} - Preset When Triggered: ${flashOnTriggerPreset}<br>"
            } else {
                state.theCogNotifications -= "<b>-</b> Use The Flasher: ${useTheFlasher} - Device: ${theFlasherDevice} - Preset When Triggered: ${flashOnTriggerPreset}<br>"
                app.removeSetting("theFlasherDevice")
                app.removeSetting("flashOnTriggerPreset")
            }
        }
    }
}

def copyCogOptions() {
    dynamicPage(name: "copyCogOptions", title: "", install:false, uninstall:false) {
		display()
        state.theMessage = ""        
        section(getFormat("header-green", "${getImage("Blank")}"+" Cog Options")) {
            paragraph "<b>Copy another Cog to this Cog!</b><br>This will overwrite all settings on this Cog with the settings of the 'from' Cog."
            paragraph "When the copy switch is turned on:<br> - It will only take a few seconds<br> - When complete the switch will turn off<br> - The app number will be blank<br> - At this point you can press 'Next'"
            paragraph "<b>Note:</b> Devices will be carried over to the new Cog but the attribute will have to be re-selected. This may cause errors in the log, just remember to go into each line and make sure the device attribute is set to the correct attribute."
        }
        section() {
            input "fromCog", "number", title: "Cog App Number to Copy <b>From</b>", submitOnChange:true, width:6
            
            if(fromCog) {
                input "copyCog", "bool", defaultValue: "false", title: "Copy Cog Now", description: "Copy Cog Now", submitOnChange: true
                if(copyCog) requestCogCopy()
            }
            paragraph "${state.theMessage}"
        }
    }
}

def installed() {
    //log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {	
    //if(logEnable) log.debug "Updated with settings: ${settings}"
    unschedule()
    unsubscribe()
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite:false])
    initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(startTime) schedule(startTime, certainTime)
        if(accelerationEvent) subscribe(accelerationEvent, "accelerationSensor", startTheProcess) 
        if(batteryEvent) subscribe(batteryEvent, "battery", startTheProcess)
        if(contactEvent) subscribe(contactEvent, "contact", startTheProcess)
        if(energyEvent) subscribe(energyEvent, "energy", startTheProcess)
        if(garagedoorEvent) subscribe(garagedoorEvent, "door", startTheProcess)
        if(hsmAlertEvent) subscribe(location, "hsmAlert", startTheProcess)
        if(hsmStatusEvent) subscribe(location, "hsmStatus", startTheProcess)
        if(humidityEvent) subscribe(humidityEvent, "humidity", startTheProcess)
        if(illuminanceEvent) subscribe(illuminanceEvent, "illuminance", startTheProcess)
        if(lockEvent) subscribe(lockEvent, "lock", startTheProcess)
        if(modeEvent && modeMatchConditionOnly == false) subscribe(location, "mode", startTheProcess)
        if(motionEvent) subscribe(motionEvent, "motion", startTheProcess)
        if(powerEvent) subscribe(powerEvent, "power", startTheProcess)
        if(presenceEvent) subscribe(presenceEvent, "presence", startTheProcess)
        if(switchEvent) subscribe(switchEvent, "switch", startTheProcess)
        if(voltageEvent) subscribe(voltageEvent, "voltage", startTheProcess) 
        if(tempEvent) subscribe(tempEvent, "temperature", startTheProcess)
        if(thermoEvent) subscribe(thermoEvent, "thermostatOperatingState", startTheProcess) 
        if(customEvent) subscribe(customEvent, specialAtt, startTheProcess)
        
        if(myContacts2) subscribe(myContacts2, "contact.closed", startTheProcess)
        if(myMotion2) subscribe(myMotion2, "motion.inactive", startTheProcess)
        if(myPresence2) subscribe(myPresence2, "presence.not present", startTheProcess)
        if(mySwitches2) subscribe(mySwitches2, "switch.off", startTheProcess)
        
        if(repeat) {
            startTheProcess()
            def rT = repeatType.toString().replace("[", "").replace("]", "")
            if(logEnable) log.debug "In initialize - repeat - repeatType: ${rT}"
            if(rT == "r1min") { 
                runEvery1Minute(startTheProcess)
            } else if(rT == "r5min") {
                runEvery5Minutes(startTheProcess)
            } else if(rT == "r10min") {
                runEvery10Minutes(startTheProcess) 
            } else if(rT == "r15min") {
                runEvery15Minutes(startTheProcess)
            } else if(rT == "r30min") {
                runEvery30Minutes(startTheProcess)
            } else if(rT == "r1hour") {
                runEvery1Hour(startTheProcess)
            } else if(rT == "r3hour") {
                runEvery3Hours(startTheProcess)
            } else {
                if(logEnable) log.debug "In initialize - repeat - Not repeating"
            }
        }       

        if(timeDaysType) {
            if(timeDaysType.contains("tPeriodic")) { 
                if(logEnable) log.debug "In initialize - tPeriodic - Creating Cron Jobs"
                if(preMadePeriodic) { schedule(preMadePeriodic, runAtTime1) }
                if(preMadePeriodic2) { schedule(preMadePeriodic2, runAtTime2) }
            }
        }
        autoSunHandler()
        checkTimeBetween()

        if(runNow) {
            app.updateSetting("runNow",[value:"false",type:"bool"])
            startTheProcess()
        }
    }
}

def startTheProcess(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.trace "*"
        if(logEnable) log.trace "******************** Start - startTheProcess (${state.version}) - ${app.label} ********************"
        state.totalMatch = 0
        state.totalMatchHelper = 0
        state.totalConditions = 0
        state.rCount = 0
        state.restrictionMatch = 0
        state.isThereDevices = false
        state.isThereSPDevices = false
        state.areRestrictions = false
        state.setpointLow = null
        state.setpointHigh = null
        state.whoText = ""
        if(startTime || preMadePeriodic) {
            state.totalMatch = 1
            state.totalConditions = 1
        }
        
        if(evt) {
            if(evt == "runAfterDelay") {
                state.whoHappened = "NA"
                state.whatHappened = "NA"
            } else if(evt == "timeReverse" || evt == "reverse") {
                state.whatToDo = "skipToReverse"
            } else if(evt == "run") {
                state.whatToDo = "run"
            } else {
                try {
                    state.whoHappened = evt.displayName
                    state.whatHappened = evt.value
                    state.whoText = evt.descriptionText
                } catch(e) {
                    if(logEnable) log.debug "In startTheProcess - Whoops!"
                }
                if(logEnable) log.debug "In startTheProcess - whoHappened: ${state.whoHappened} - whatHappened: ${state.whatHappened} - whoText: ${state.whoText}"
                state.hasntDelayedYet = true
                state.hasntDelayedReverseYet = true
                state.whatToDo = "run"
            }
        } else {
            state.whatToDo = "run"
            state.whoHappened = ""
            state.whatHappened = ""
            state.whoText = ""
        }
        accelerationRestrictionHandler()
        contactRestrictionHandler()
        garageDoorRestrictionHandler()
        lockRestrictionHandler()
        motionRestrictionHandler()
        presenceRestrictionHandler()
        switchRestrictionHandler()
        waterRestrictionHandler()

        if(state.areRestrictions) {
            if(logEnable) log.debug "In startTheProcess - whatToDo: ${state.whatToDo} - Restrictions are true, skipping"
            state.whatToDo = "stop"
        } else {
            if(state.whatToDo == "stop" || state.whatToDo == "skipToReverse") {
                if(logEnable) log.debug "In startTheProcess - Skipping Time checks - whatToDo: ${state.whatToDo}"
            } else {
                checkTimeBetween()
                checkTimeSun()
                dayOfTheWeekHandler()
                modeHandler()
                hsmAlertHandler(state.whatHappened)
                hsmStatusHandler(state.whatHappened)
                if(logEnable) log.debug "In startTheProcess - 1A - betweenTime: ${state.betweenTime} - timeBetweenSun: ${state.timeBetweenSun} - daysMatch: ${state.daysMatch} - modeMatch: ${state.modeMatch}"
                if(daysMatchRestriction && !state.daysMatch) { state.whatToDo = "stop" }
                if(timeBetweenRestriction && !state.betweenTime) { state.whatToDo = "stop" }
                if(timeBetweenSunRestriction && !state.timeBetweenSun) { state.whatToDo = "stop" } 
                if(modeMatchRestriction && !state.modeMatch) { state.whatToDo = "stop" }
            }           
            if(logEnable) log.debug "In startTheProcess - 1B - daysMatchRestic: ${daysMatchRestriction} - timeBetweenRestric: ${timeBetweenRestriction} - timeBetweenSunRestric: ${timeBetweenSunRestriction} - modeMatchRestric: ${modeMatchRestriction}"          
            if(logEnable) log.debug "In startTheProcess - 1C - betweenTime: ${state.betweenTime} - timeBetweenSun: ${state.timeBetweenSun} - daysMatch: ${state.daysMatch} - modeMatch: ${state.modeMatch}"
            
            if(state.whatToDo == "stop" || state.whatToDo == "skipToReverse") {
                if(logEnable) log.debug "In startTheProcess - Skipping Device checks - whatToDo: ${state.whatToDo}"
            } else {
                accelerationHandler()
                contactHandler()
                contact2Handler()
                garageDoorHandler()
                lockHandler()
                motionHandler()
                motion2Handler()
                presenceHandler()
                presence2Handler()
                switchHandler()
                switch2Handler()
                thermostatHandler()
                waterHandler()

                batteryHandler()
                energyHandler()
                humidityHandler()
                illuminanceHandler()
                powerHandler()
                tempHandler()
                voltageHandler()
                
                if(deviceORsetpoint) {
                    customSetpointHandler()
                } else {
                    customDeviceHandler()
                }
                
                if(gvStyle) { 
                    globalVariablesNumberHandler()
                } else {
                    globalVariablesTextHandler() 
                }
                checkingWhatToDo()            
            }
        }

        if(state.whatToDo == "stop") {
            if(logEnable) log.debug "In startTheProcess - Nothing to do - STOPING - whatToDo: ${state.whatToDo}"
        } else {
            if(state.whatToDo == "run") {
                if(state.modeMatch && state.daysMatch && state.betweenTime && state.timeBetweenSun && state.modeMatch) {
                    if(logEnable) log.debug "In startTheProcess - HERE WE GO! - whatToDo: ${state.whatToDo}"
                    if(state.hasntDelayedYet == null) state.hasntDelayedYet = false
                    if((notifyDelay || randomDelay || targetDelay) && state.hasntDelayedYet) {
                        if(notifyDelay && minSec) {
                            theDelay = notifyDelay
                            if(logEnable) log.debug "In startTheProcess - Delay is set for ${notifyDelay} second(s)"
                        } else if(notifyDelay && !minSec) {
                            theDelay = notifyDelay * 60
                            if(logEnable) log.debug "In startTheProcess - Delay is set for ${notifyDelay} minute(s)"
                        } else if(randomDelay) {
                            newDelay = Math.abs(new Random().nextInt() % (delayHigh - delayLow)) + delayLow
                            theDelay = newDelay * 60
                            if(logEnable) log.debug "In startTheProcess - Delay is set for ${newDelay} minute(s)"
                        } else if(targetDelay) {
                            theDelay = minutesUp * 60
                            if(logEnable) log.debug "In startTheProcess - Delay is set for ${minutesUp} minute(s)"
                        } else {
                            if(logEnable) log.warn "In startTheProcess - Something went wrong"
                        }
                        if(actionType) {
                            if(actionType.contains("aSwitch") && switchedDimUpAction) { slowOnHandler() }
                        }                              
                        state.hasntDelayedYet = false
                        state.setpointHighOK = "yes"
                        state.setpointLowOK = "yes"
                        state.setpointBetweenOK = "yes"
                        runIn(theDelay, startTheProcess, [data: "runAfterDelay"])
                    } else {
                        if(actionType) {
                            if(actionType.contains("aFan")) { fanActionHandler() }
                            if(actionType.contains("aGarageDoor") && (garageDoorOpenAction || garageDoorClosedAction)) { garageDoorActionHandler() }
                            if(actionType.contains("aLock") && (lockAction || unlockAction)) { lockActionHandler() }
                            if(actionType.contains("aValve") && (valveOpenAction || valveClosedAction)) { valveActionHandler() }
                            if(actionType.contains("aSwitch") && switchesOnAction) { switchesOnActionHandler() }
                            if(actionType.contains("aSwitch") && switchesOffAction && permanentDim2) { permanentDimHandler() }
                            if(actionType.contains("aSwitch") && switchesOffAction && !permanentDim2) { switchesOffActionHandler() }
                            if(actionType.contains("aSwitch") && switchesToggleAction) { switchesToggleActionHandler() }
                            if(actionType.contains("aSwitch") && setOnLC) { dimmerOnActionHandler() }
                            if(actionType.contains("aSwitch") && switchedDimDnAction) { slowOffHandler() }
                            if(actionType.contains("aSwitch") && switchedDimUpAction) { slowOnHandler() }
                            if(actionType.contains("aSwitchSequence")) { switchesInSequenceHandler() }
                            if(actionType.contains("aSwitchesPerMode") && setDimmersPerMode) { switchesPerModeActionHandler() }
                            if(actionType.contains("aThermostat")) { thermostatActionHandler() }
                            //if(actionType.contains("aSendHTTP")) { sendHTTPActionHandler(httpPath, httpMethod) }
                            if(actionType.contains("aSendHTTP")) { runCmd(httpPath, httpMethod) }
                            if(actionType.contains("aNotification")) { 
                                state.doMessage = true
                                messageHandler() 
                                if(useTheFlasher) theFlasherHandler()
                            }
                            if(actionType.contains("aVirtualContact") && (contactOpenAction || contactClosedAction)) { contactActionHandler() }
                        }
                        if(setHSM) hsmChangeActionHandler()
                        if(modeAction) modeChangeActionHandler()
                        if(devicesToRefresh) devicesToRefreshActionHandler()
                        if(rmRule) ruleMachineHandler()
                        if(setGVname && setGVvalue) setGlobalVariableHandler()
                        state.hasntDelayedYet = true
                        if(timeReverse) {
                            theDelay = timeToReverse * 60
                            if(logEnable) log.debug "In startTheProcess - Reverse will run in ${timeToReverse} minutes"
                            runIn(theDelay, startTheProcess, [data: "timeReverse"])
                        }
                    }
                    state.appStatus = "active"
                } else {
                    if(logEnable) log.debug "In startTheProcess - One of the Time Conditions didn't match - Stopping"
                }
            } else if(state.whatToDo == "reverse" || state.whatToDo == "skipToReverse") {
                if(reverseWithDelay && state.hasntDelayedReverseYet) {
                    if(reverseWithDelay) {
                        timeToReverse = timeToReverse ?: 1
                        theDelay = timeToReverse * 60
                        if(logEnable) log.debug "In startTheProcess - Reverse - Delay is set for ${timeToReverse} minute(s)"
                    } else {
                        if(logEnable) log.warn "In startTheProcess - Reverse - Something went wrong"
                    }
                    state.hasntDelayedReverseYet = false
                    if(dimWhileDelayed && (state.appStatus == "active")) { permanentDimHandler() }
                    runIn(theDelay, startTheProcess, [data: "runAfterDelay"])
                } else {             
                    if(logEnable) log.debug "In startTheProcess - GOING IN REVERSE"
                    if(actionType) {
                        if(actionType.contains("aFan")) { fanReverseActionHandler() }
                        if(actionType.contains("aSwitch") && switchesOnAction) { switchesOnReverseActionHandler() }
                        if(actionType.contains("aSwitch") && switchesOffAction && permanentDim2) { permanentDimHandler() }
                        if(actionType.contains("aSwitch") && switchesOffAction && !permanentDim2) { switchesOffReverseActionHandler() }
                        if(actionType.contains("aSwitch") && switchesToggleAction) { switchesToggleActionHandler() }
                        if(actionType.contains("aSwitch") && setOnLC && permanentDim) { permanentDimHandler() }
                        if(actionType.contains("aSwitch") && setOnLC && !permanentDim) { dimmerOnReverseActionHandler() }  
                        if(actionType.contains("aSwitchSequence")) { switchesInSequenceReverseHandler() }
                        if(actionType.contains("aSwitchesPerMode") && setDimmersPerMode && permanentDim) { permanentDimHandler() }
                        if(actionType.contains("aSwitchesPerMode") && setDimmersPerMode && !permanentDim) { switchesPerModeReverseActionHandler() }
                        if(batteryEvent || humidityEvent || illuminanceEvent || powerEvent || tempEvent || (customEvent && deviceORsetpoint)) {
                            if(actionType.contains("aNotification")) { 
                                state.doMessage = true
                                messageHandler() 
                                if(useTheFlasher) theFlasherHandler()
                            }
                        }
                        if(actionType.contains("aVirtualContact") && (contactOpenAction || contactClosedAction)) { contactReverseActionHandler() }
                    }
                    state.hasntDelayedReverseYet = true
                }
                state.appStatus = "inactive"
            } else {
                if(logEnable) log.debug "In startTheProcess - Something isn't right - STOPING"
            }
        }
        if(logEnable) log.trace "********************* End - startTheProcess (${state.version}) - ${app.label} *********************"
        if(logEnable) log.trace "*"
    }
}

// ********** Start Conditions **********
/* add
app.removeSetting("myPresence2")
*/
def customDeviceHandler() {
    if(customEvent) {
        state.eventName = customEvent
        state.eventType = specialAtt
        state.type = sdCustom1Custom2
        state.typeValue1 = custom1
        state.typeValue2 = custom2
        state.typeAO = customANDOR
        devicesGoodHandler("condition")
    }
}
def accelerationHandler() {
    if(accelerationEvent) {
        state.eventName = accelerationEvent
        state.eventType = "acceleration"
        state.type = asInactiveActive
        state.typeValue1 = "active"
        state.typeValue2 = "inactive"
        state.typeAO = accelerationANDOR
        devicesGoodHandler("condition")
    }
}
def contactHandler() {
    if(contactEvent) {
        state.eventName = contactEvent
        state.eventType = "contact"
        state.type = csClosedOpen
        state.typeValue1 = "open"
        state.typeValue2 = "closed"
        state.typeAO = contactANDOR
        devicesGoodHandler("condition")
    }
}
def contact2Handler() {
    if(myContacts2) {
        state.eventName = myContacts2
        state.eventType = "contact"
        state.type = contactOption2
        state.typeValue1 = "open"
        state.typeValue2 = "closed"
        state.typeAO = true
        devicesGoodHandler("helper")
    }
}
def garageDoorHandler() {
    if(garageDoorEvent) {
        state.eventName = garageDoorEvent
        state.eventType = "door"
        state.type = gdClosedOpen
        state.typeValue1 = "open"
        state.typeValue2 = "closed"
        state.typeAO = garageDoorANDOR
        devicesGoodHandler("condition")
    }
}
def globalVariablesTextHandler() {
    if(globalVariableEvent) {
        state.eventName = globalVariableEvent
        state.eventType = "globalVariable"
        state.type = true
        state.typeValue1 = gvValue
        state.typeValue2 = "noData"
        state.typeAO = false
        devicesGoodHandler("condition")
    }
}
def lockHandler() {
    if(lockEvent) {
        state.eventName = lockEvent
        state.eventType = "lock"
        state.type = lUnlockedLocked
        state.typeValue1 = "locked"
        state.typeValue2 = "unlocked"
        state.typeAO = lockANDOR
        devicesGoodHandler("condition")
    }
}
def motionHandler() {
    if(motionEvent) {
        state.eventName = motionEvent
        state.eventType = "motion"
        state.type = meInactiveActive
        state.typeValue1 = "active"
        state.typeValue2 = "inactive"
        state.typeAO = motionANDOR
        devicesGoodHandler("condition")
    }
}
def motion2Handler() {
    if(myMotion2) {
        state.eventName = myMotion2
        state.eventType = "motion"
        state.type = motionOption2
        state.typeValue1 = "active"
        state.typeValue2 = "inactive"
        state.typeAO = true
        devicesGoodHandler("helper")
    }
}
def presenceHandler() {
    if(presenceEvent) {
        state.eventName = presenceEvent
        state.eventType = "presence"
        state.type = pePresentNotPresent
        state.typeValue1 = "not present"
        state.typeValue2 = "present"
        state.typeAO = presenceANDOR
        devicesGoodHandler("condition")
    }
}
def presence2Handler() {
    if(myPresence2) {
        state.eventName = myPresence2
        state.eventType = "presence"
        state.type = presenceOption2
        state.typeValue1 = "not present"
        state.typeValue2 = "present"
        state.typeAO = true
        devicesGoodHandler("helper")
    }
}
def switchHandler() {
    if(switchEvent) {
        state.eventName = switchEvent
        state.eventType = "switch"
        state.type = seOffOn
        state.typeValue1 = "on"
        state.typeValue2 = "off"
        state.typeAO = switchANDOR
        devicesGoodHandler("condition")
    }
}
def switch2Handler() {
    if(mySwitches2) {
        state.eventName = mySwitches2
        state.eventType = "switch"
        state.type = switchesOption2
        state.typeValue1 = "on"
        state.typeValue2 = "off"
        state.typeAO = true
        devicesGoodHandler("helper")
    }
}
def thermostatHandler() {
    if(thermoEvent) {
        state.eventName = thermoEvent
        state.eventType = "thermostatOperatingState"
        state.type = false
        state.typeValue1 = "idle"
        state.typeValue2 = "thermostatEvent"
        state.typeAO = thermoANDOR
        devicesGoodHandler("condition")
    }
}
def waterHandler() {
    if(waterEvent) {
        state.eventName = waterEvent
        state.eventType = "water"
        state.type = weDryWet
        state.typeValue1 = "Wet"
        state.typeValue2 = "Dry"
        state.typeAO = waterANDOR
        devicesGoodHandler("condition")
    }
}

def devicesGoodHandler(data) {
    if(logEnable) log.debug "In devicesGoodHandler (${state.version}) - ${state.eventType.toUpperCase()} - data: ${data}"
    state.deviceMatch = 0
    state.count = 0
    deviceTrue1 = 0
    deviceTrue2 = 0
    state.isThereDevices = true
    if(data == "condition") { state.totalConditions = state.totalConditions + 1 }
    try {
        if(state.eventType == "globalVariable") {
            theList = []
            theList << globalVariableEvent
            state.eventName = theList
            state.theCount = state.eventName.size()
        } else {
            state.theCount = state.eventName.size()
        }
    } catch(e) { 
        state.theCount = 1
    }
    if(state.whoText == null) state.whoText = ""
    if(state.eventName) {
        state.eventName.each { it ->
            if(state.eventType == "globalVariable") {
                def theData = state.gvMap.get(globalVariableEvent)
                theValue = theData.toString()
            } else {
                theValue = it.currentValue("${state.eventType}").toString()
            }
            if(logEnable) log.debug "In devicesGoodHandler - Checking: ${it.displayName} - ${state.eventType} - Testing Current Value - ${theValue}"
            if(theValue == state.typeValue1) {
                if(logEnable) log.debug "In devicesGoodHandler - Working 1: ${state.typeValue1} and Current Value: ${theValue}"
                if(state.eventType == "switch") {
                    if(seType) {
                        if(logEnable) log.trace "In devicesGoodHandler - Switch - Only Physical"
                        if(state.whoText.contains("[physical]")) { deviceTrue1 = deviceTrue1 + 1 }
                    } else {
                        if(logEnable) log.trace "In devicesGoodHandler - Switch - Digital and Physical"
                        deviceTrue1 = deviceTrue1 + 1
                    }  
                } else {
                    deviceTrue1 = deviceTrue1 + 1
                }
            } else if(theValue == state.typeValue2) { 
                if(logEnable) log.debug "In devicesGoodHandler - Working 2: ${state.typeValue2} and Current Value: ${theValue}"
                if(state.eventType == "lock") {
                    if(state.whoText.contains("unlocked by")) {
                        if(lockUser) {
                            state.whoUnlocked = it.currentValue("lastCodeName")
                            lockUser.each { us ->
                                if(logEnable && logSize) log.debug "Checking lock names - $us vs $state.whoUnlocked"
                                if(us == state.whoUnlocked) { 
                                    if(logEnable && logSize) log.debug "MATCH: ${state.whoUnlocked}"
                                    deviceTrue2 = deviceTrue2 + 1
                                }
                            }
                        } else {
                            if(logEnable) log.trace "In devicesGoodHandler - No user selected, no notifications necessary"
                            deviceTrue2 = deviceTrue2 + 1
                        }
                    } else {
                        if(logEnable) log.trace "In devicesGoodHandler - Lock was manually unlocked, no notifications necessary"
                        deviceTrue2 = deviceTrue2 + 1
                    }
                } else if(state.eventType == "switch") {
                    if(seType) {
                        if(logEnable) log.trace "In devicesGoodHandler - Switch - Only Physical"
                        if(state.whoText.contains("[physical]")) { deviceTrue2 = deviceTrue2 + 1 }
                    } else {
                        if(logEnable) log.trace "In devicesGoodHandler - Switch - Digital and Physical"
                        deviceTrue2 = deviceTrue2 + 1
                    }  
                } else {
                    deviceTrue2 = deviceTrue2 + 1
                }
            } else {
                if(state.eventType == "thermostatOperatingState") {
                    if(theValue != "idle") {
                        deviceTrue2 = deviceTrue2 + 1
                        if(logEnable && logSize) log.debug "In devicesGoodHandler - Thermostat - Working 2: Current Value: ${theValue}"
                    }
                } else {
                    // next option
                }
            }
        }
    }
    if(state.type) {
        state.deviceMatch = state.deviceMatch + deviceTrue1
    } else {
        state.deviceMatch = state.deviceMatch + deviceTrue2
    }
    if(logEnable) log.debug "In devicesGoodHandler - deviceMatch: ${state.deviceMatch} - theCount: ${state.theCount} - type: ${state.typeAO}" 
    if(state.typeAO) {  // OR (true)
        if(state.deviceMatch >= 1) {
            if(logEnable) log.debug "In devicesGoodHandler - Using OR1"
            if(data == "condition") { state.totalMatch = state.totalMatch + 1 }
            if(data == "helper") { state.totalMatchHelper = state.totalMatchHelper + 1 }
        }
    } else {  // AND (False)
        if(state.deviceMatch == state.theCount) {
            if(logEnable) log.debug "In devicesGoodHandler - Using AND1"
            if(data == "condition") { state.totalMatch = state.totalMatch + 1 }
            if(data == "helper") { state.totalMatchHelper = state.totalMatchHelper + 1 }
        }
    }
    if(state.typeAO) {
        if(logEnable) log.debug "In devicesGoodHandler - ${state.eventType.toUpperCase()} - OR - count: ${state.theCount} - totalMatch: ${state.totalMatch} - totalConditions: ${state.totalConditions}"
    } else {
        if(logEnable) log.debug "In devicesGoodHandler - ${state.eventType.toUpperCase()} - AND - count: ${state.theCount} - totalMatch: ${state.totalMatch} - totalConditions: ${state.totalConditions}"
    }
}

def hsmAlertHandler(data) {
    if(hsmAlertEvent) {
        if(logEnable) log.debug "In hsmAlertHandler (${state.version})"
        String theValue = data
        hsmAlertEvent.each { it ->
            if(logEnable && logSize) log.debug "In hsmAlertHandler - Checking: ${it} - value: ${theValue}"
            if(theValue == it){
                state.totalMatch = 1
                state.totalConditions = 1
            }
        }
    }
    if(logEnable) log.debug "In hsmAlertHandler - hsmAlertStatus: ${state.hsmAlertStatus}"
}

def hsmStatusHandler(data) {
    if(hsmStatusEvent) {
        if(logEnable) log.debug "In hsmStatusHandler (${state.version})"
        String theValue = data
        hsmStatusEvent.each { it ->
            if(logEnable && logSize) log.debug "In hsmStatusHandler - Checking: ${it} - value: ${theValue}"
            if(theValue == it){
                state.totalMatch = 1
                state.totalConditions = 1
            }
        }
    }
    if(logEnable) log.debug "In hsmStatusHandler - hsmStatus: ${state.hsmStatus}"
}

def ruleMachineHandler() {
    if(logEnable) log.debug "In ruleMachineHandler - Rule: ${rmRule} - Action: ${rmAction}"
    RMUtils.sendAction(rmRule, rmAction, app.label)
}

// ***** Start Setpoint Handlers *****
def customSetpointHandler() {
    if(customEvent) {
        state.spName = customEvent
        state.spType = specialAtt
        state.setpointHigh = sdSetPointHigh
        state.setpointLow = sdSetPointLow
        setpointHandler()
    }
}
def batteryHandler() {
    if(batteryEvent) {
        state.spName = batteryEvent
        state.spType = "battery"
        state.setpointHigh = beSetPointHigh
        state.setpointLow = beSetPointLow
        setpointHandler()
    }
}
def energyHandler() {
    if(energyEvent) {
        state.spName = energyEvent
        state.spType = "energy"
        state.setpointHigh = eeSetPointHigh
        state.setpointLow = eeSetPointLow
        setpointHandler()
    }
}
def globalVariablesNumberHandler() {
    if(globalVariableEvent) {
        state.spName = globalVariableEvent
        state.spType = "globalVariable"
        state.setpointHigh = gvSetPointHigh
        state.setpointLow = gvSetPointLow
        setpointHandler()
    }
}
def humidityHandler() {
    if(humidityEvent) {
        state.spName = humidityEvent
        state.spType = "humidity"
        state.setpointHigh = heSetPointHigh
        state.setpointLow = heSetPointLow
        setpointHandler()
    } 
}
def illuminanceHandler() {
    if(illuminanceEvent) {
        state.spName = illuminanceEvent
        state.spType = "illuminance"
        state.setpointHigh = ieSetPointHigh
        state.setpointLow = ieSetPointLow
        setpointHandler()
    }
}
def powerHandler() {
    if(powerEvent) {
        state.spName = powerEvent
        state.spType = "power"
        state.setpointHigh = peSetPointHigh
        state.setpointLow = peSetPointLow
        setpointHandler()
    }
}
def tempHandler() {
    if(tempEvent) {
        state.spName = tempEvent
        state.spType = "temperature"
        state.setpointHigh = teSetPointHigh
        state.setpointLow = teSetPointLow
        setpointHandler()
    }
}
def voltageHandler() {
    if(voltageEvent) {
        state.spName = voltageEvent
        state.spType = "voltage"
        state.setpointHigh = veSetPointHigh
        state.setpointLow = veSetPointLow
        setpointHandler()
    }    
    if(!state.isThereSPDevices) {  // Keep in LAST setpoint
        if(triggerAndOr) {
            state.setpointOK = false
        } else {
            state.setpointOK = true
        }
        state.setpointHighOK = "yes"
        state.setpointLowOK = "yes"
        state.setpointBetweenOK = "yes"
    }
}

def setpointHandler() {
    if(logEnable) log.debug "In setpointHandler (${state.version}) - spName: ${state.spName}"
    if(state.setpointHighOK == null) state.setpointHighOK = "yes"
    if(state.setpointLowOK == null) state.setpointLowOK = "yes"
    if(state.setpointBetweenOK == null) state.setpointBetweenOK = "yes"
    state.isThereSPDevices = true
    state.spName.each {
        if(state.spType == "globalVariable") {
            def theData = state.gvMap.get(globalVariableEvent)
            spValue = theData
            if(logEnable) log.debug "In setpointHandler - theData: ${theData}"
        } else {
            spValue = it.currentValue("${state.spType}")
        }
        if(logEnable) log.debug "In setpointHandler - spValue: ${spValue}"
        if(spValue) {
            if(useWholeNumber) {
                setpointValue = Math.round(spValue)
            } else {
                setpointValue = spValue.toDouble()
            }
            state.preSPV = setpointValue
            int setpointValue = setpointValue
            if(setpointRollingAverage) {
                if(state.readings == null) state.readings = []
                state.readings.add(0,setpointValue)           
                int maxReadingSize = state.spName.size() * numOfPoints
                int readings = state.readings.size()            
                if(readings > maxReadingSize) state.readings.removeAt(maxReadingSize)
                setpointRollingAverageHandler(maxReadingSize)
                if(state.theAverage >= 0) setpointValue = state.theAverage
            }
            if(state.setpointHigh && state.setpointLow) {
                int setpointLow = state.setpointLow
                int setpointHigh = state.setpointHigh
                if(setpointValue <= setpointHigh && setpointValue > setpointLow) {
                    if(logEnable) log.debug "In setpointHandler (Between) - Device: ${it}, Value: ${setpointValue} is BETWEEN setpointHigh: ${setpointHigh} and setpointLow: ${setpointLow}"
                    state.setpointBetweenOK = "no"
                    state.setpointOK = true
                } else {
                    state.setpointBetweenOK = "yes"
                    state.setpointOK = false
                }
            } else if(state.setpointHigh) {
                int setpointHigh = state.setpointHigh
                if(setpointValue >= setpointHigh) {  // bad
                    if(logEnable) log.debug "In setpointHandler (High) - Device: ${it}, Value: ${setpointValue} is GREATER THAN setpointHigh: ${setpointHigh} (Bad)"
                    state.setpointHighOK = "no"
                    state.setpointOK = true
                } else {
                    if(logEnable) log.debug "In setpointHandler (High) - Device: ${it}, Value: ${setpointValue} is LESS THAN setpointHigh: ${setpointHigh} (Good)"
                    state.setpointHighOK = "yes"
                    state.setpointOK = false
                }
            } else if(state.setpointLow) {
                int setpointLow = state.setpointLow
                if(setpointValue < setpointLow) {  // bad
                    if(logEnable) log.debug "In setpointHandler (Low) - Device: ${it}, Value: ${setpointValue} is LESS THAN setpointLow: ${setpointLow} (Bad)"
                    state.setpointLowOK = "no"
                    state.setpointOK = true
                } else {
                    if(logEnable) log.debug "In setpointHandler (Low) - Device: ${it}, Value: ${setpointValue} is GREATER THAN setpointLow: ${setpointLow} (Good)"
                    state.setpointLowOK = "yes"
                    state.setpointOK = false
                }
            }
        } else {
            if(state.setpointHigh && state.setpointLow) {
                state.setpointBetweenOK = "no"
                state.setpointOK = true
            } else if(state.setpointHigh) {
                state.setpointHighOK = "no"
                state.setpointOK = true
            } else if(state.setpointLow) {  
                state.setpointLowOK = "no"
                state.setpointOK = true
            }
        }
    }
    if(logEnable) log.debug "In setpointHandler - ${state.spType.toUpperCase()} - setpointOK: ${state.setpointOK}"
}

def setpointRollingAverageHandler(data) {
    if(logEnable) log.debug "In setpointRollingAverageHandler (${state.version})"
    int totalNum = 0
    int maxReadingSize = data    
    floatingPoint = false
    if(logEnable && logSize) log.debug "In setpointRollingAverageHandler  - state.readings: ${state.readings}"
    String readings = state.readings
    def theNumbers = readings.split(",")
    int readingsSize = theNumbers.size()
    if(readingsSize > 1) {       
        for(x=0;x<readingsSize;x++) {
            int theNumber = theNumbers[x].replace("[","").replace("]","").toInteger()
            if(logEnable && logSize) log.debug "In setpointRollingAverageHandler - ${x} - ${theNumber}"
            totalNum = totalNum + theNumber        
        }       
        if(logEnable && logSize) log.debug "In setpointRollingAverageHandler - totalNum: ${totalNum} - readingsSize: ${readingsSize}"
        if(totalNum == 0 || totalNum == null) {
            state.theAverage = 0
        } else {
            state.theAverage = (totalNum / readingsSize).toDouble().round()
        }
    }
    if(logEnable) log.debug "In setpointRollingAverageHandler - theAverage: ${state.theAverage} - readingSize: ${readingsSize} - readings: ${readings}"
}

// ********** Start Restrictions **********
def accelerationRestrictionHandler() {
    if(accelerationRestrictionEvent) {
        state.rEventName = accelerationRestrictionEvent
        state.rEventType = "acceleration"
        state.rType = arInactiveActive
        state.rTypeValue1 = "active"
        state.rTypeValue2 = "inactive"
        state.rTypeAO = accelerationRANDOR
        restrictionHandler()
    }
}
def contactRestrictionHandler() {
    if(contactRestrictionEvent) {
        state.rEventName = contactRestrictionEvent
        state.rEventType = "contact"
        state.rType = crClosedOpen
        state.rTypeValue1 = "open"
        state.rTypeValue2 = "closed"
        state.rTypeAO = contactRANDOR
        restrictionHandler()
    } 
}
def garageDoorRestrictionHandler() {
    if(garageDoorRestrictionEvent) {
        state.rEventName = garageDoorRestrictionEvent
        state.rEventType = "door"
        state.rEype = gdrClosedOpen
        state.rTypeValue1 = "open"
        state.rTypeValue2 = "closed"
        state.rTypeAO = garageDoorRANDOR
        restrictionHandler()
    } 
}
def lockRestrictionHandler() {
    if(lockRestrictionEvent) {
        state.rEventName = lockRestrictionEvent
        state.rEventType = "lock"
        state.rType = lrUnlockedLocked
        state.rTypeValue1 = "locked"
        state.rTypeValue2 = "unlocked"
        state.rTypeAO = false
        restrictionHandler()
    } 
}
def motionRestrictionHandler() {
    if(motionRestrictionEvent) {
        state.rEventName = motionRestrictionEvent
        state.rEventType = "motion"
        state.rType = mrInactiveActive
        state.rTypeValue1 = "active"
        state.rTypeValue2 = "inactive"
        state.rTypeAO = motionRANDOR
        restrictionHandler()
    }
}
def presenceRestrictionHandler() {
    if(presenceRestrictionEvent) {
        state.rEventName = presenceRestrictionEvent
        state.rEventType = "presence"
        state.rType = prPresentNotPresent
        state.rTypeValue1 = "not present"
        state.rTypeValue2 = "present"
        state.rTypeAO = presenceRANDOR
        restrictionHandler()
    }
}
def switchRestrictionHandler() {
    if(switchRestrictionEvent) {
        state.rEventName = switchRestrictionEvent
        state.rEventType = "switch"
        state.rType = srOffOn
        state.rTypeValue1 = "on"
        state.rTypeValue2 = "off"
        state.rTypeAO = switchRANDOR
        restrictionHandler()
    }
}
def waterRestrictionHandler() {
    if(waterRestrictionEvent) {
        state.rEventName = waterRestrictionEvent
        state.rEventType = "water"
        state.rType = wrDryWet
        state.rTypeValue1 = "Wet"
        state.rTypeValue2 = "Dry"
        state.rTypeAO = waterRANDOR
        restrictionHandler()
    }
}

def restrictionHandler() {
    if(logEnable) log.debug "In restrictionHandler (${state.version}) - ${state.rEventType.toUpperCase()}"
    restrictionMatch1 = 0
    restrictionMatch2 = 0
    try {
        theCount = state.rEventName.size()
    } catch(e) {
        theCount = 1
    }
    state.rCount = state.rCount + theCount
    state.rEventName.each { it ->
        theValue = it.currentValue("${state.rEventType}")
        if(logEnable && logSize) log.debug "In restrictionHandler - Checking: ${it.displayName} - ${state.rEventType} - Testing Current Value - ${theValue}"
        if(theValue == state.rTypeValue1) { 
            if(state.rEventType == "lock") {
                if(logEnable && logSize) log.debug "In restrictionHandler - Lock"
                state.whoUnlocked = it.currentValue("lastCodeName")
                lockRestrictionUser.each { us ->
                    if(logEnable && logSize) log.debug "Checking lock names - $us vs $state.whoUnlocked"
                    if(us == state.whoUnlocked) { 
                        if(logEnable && logSize) log.debug "MATCH: ${state.whoUnlocked}"
                        restrictionMatch1 = restrictionMatch1 + 1
                    }
                }
            } else {
                if(logEnable && logSize) log.debug "In restrictionHandler - Everything Else 1"
                restrictionMatch1 = restrictionMatch1 + 1
            }
        } else if(theValue == state.rTypeValue2) { 
            if(state.rEventType == "lock") {
                state.whoUnlocked = it.currentValue("lastCodeName")
                lockRestrictionUser.each { us ->
                    if(logEnable && logSize) log.debug "Checking lock names - $us vs $state.whoUnlocked"
                    if(us == state.whoUnlocked) { 
                        if(logEnable && logSize) log.debug "MATCH: ${state.whoUnlocked}"
                        restrictionMatch2 = restrictionMatch2 + 1
                    }
                }
            } else {
                if(logEnable && logSize) log.debug "In restrictionHandler - Everything Else 2"
                restrictionMatch2 = restrictionMatch2 + 1
            }
        }
    }
    if(logEnable && logSize) log.debug "In restrictionHandler - theCount: ${theCount} - theValue: ${theValue} vs 1: ${state.restrictionMatch1} or 2: ${state.restrictionMatch2}"
    if(state.rType) {
        state.restrictionMatch = state.restrictionMatch + restrictionMatch1
    } else {
        state.restrictionMatch = state.restrictionMatch + restrictionMatch2
    }
    if(logEnable && logSize) log.debug "In devicesGoodHandler - restrictionMatch: ${state.restrictionMatch} - rCount: ${state.rCount} - type: ${state.rTypeAO}" 
    if(state.rTypeAO) {  // OR (true)
        if(state.restrictionMatch >= 1) {
            state.areRestrictions = true
        } 
    } else {  // AND (False)
        if(state.restrictionMatch == state.rCount) {
            state.areRestrictions = true
        }
    }
    if(logEnable) log.debug "In restrictionHandler - ${state.rEventType.toUpperCase()} - areRestrictions: ${state.areRestrictions}"
}

// ********** Start Actions **********
def switchesPerModeActionHandler() {
    currentMode = location.mode
    setDimmersPerMode.each { itOne ->
        def theData = "${state.sdPerModeMap}".split(",")        
        theData.each { itTwo -> 
            def (theMode, theDevice, theLevel, theTemp, theColor) = itTwo.split(":")
            if(theMode.startsWith(" ") || theMode.startsWith("[")) theMode = theMode.substring(1)
            theColor = theColor.replace("]","")           
            def cleandevice = theDevice.replace("[","").replace("]","")
            def cleanOne = "${itOne}"    
            if(cleandevice == cleanOne) {
                def modeCheck = currentMode.contains(theMode)
                if(modeCheck) {
                    state.fromWhere = "switchesPerMode"
                    state.sPDM = itOne
                    state.onColor = "${theColor}"
                    state.onLevel = theLevel
                    state.onTemp = theTemp
                    setLevelandColorHandler()
                }
            }
        }
    }
}

def switchesPerModeReverseActionHandler() {
    if(setDimmersPerMode) {
        setDimmersPerMode.each { it ->
            if(logEnable) log.debug "In switchesPerModeReverseActionHandler - Working on $it"
            def theData = "${state.sdPerModeMap}".split(",")
            theData.each { itTwo -> 
                def (theMode, theDevice, theLevel, theTemp, theColor) = itTwo.split(":")
                if(theMode.startsWith(" ") || theMode.startsWith("[")) theMode = theMode.substring(1)
                theColor = theColor.replace("]","")           
                def cleandevice = theDevice.replace("[","").replace("]","")
                def cleanOne = "${it}"
                if(cleandevice == cleanOne) {
                    currentMode = location.mode
                    def modeCheck = currentMode.contains(theMode)
                    if(modeCheck) {
                        currentONOFF = it.currentValue("switch")
                        if(currentONOFF == "on") {
                            if(it.hasCommand("setColor") && theTemp == "NA") {
                                name = (it.displayName).replace(" ","")
                                try {
                                    data = state.oldMapPer.get(name)
                                    def (oldStatus, oldHueColor, oldSaturation, oldLevel, oldColorTemp, oldColorMode) = data.split("::")
                                    int hueColor = oldHueColor.toInteger()
                                    int saturation = oldSaturation.toInteger()
                                    int level = oldLevel.toInteger()
                                    int cTemp = oldColorTemp.toInteger()
                                    def cMode = oldColorMode
                                    if(cMode == "CT") {
                                        if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColor - Reversing Light: ${it} - oldStatus: ${oldStatus} - cTemp: ${ctemp} - level: ${level}"
                                        pauseExecution(actionDelay)
                                        it.setColorTemperature(cTemp)
                                        pauseExecution(actionDelay)
                                        it.setLevel(level)                          
                                        if(oldStatus == "off") {                            
                                            if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColor - Turning light off (${it})"
                                            pauseExecution(actionDelay)
                                            it.off()
                                        }
                                    } else {
                                        def theValue = [hue: hueColor, saturation: saturation, level: level]
                                        if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColor - Reversing Light: ${it} - oldStatus: ${oldStatus} - theValue: ${theValue}"
                                        pauseExecution(actionDelay)
                                        it.setColor(theValue)
                                        if(oldStatus == "off") {
                                            if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColor - Turning light off (${it})"
                                            pauseExecution(actionDelay)
                                            it.off()
                                        }
                                    }
                                } catch(e) {
                                    if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColor - Turning Off (${it})"
                                    pauseExecution(actionDelay)
                                    it.off()
                                }
                            } else if(it.hasCommand("setColorTemperature") && theColor == "NA") {
                                name = (it.displayName).replace(" ","")
                                try {
                                    data = state.oldMapPer.get(name)
                                    def (oldStatus, oldLevel, oldTemp) = data.split("::")
                                    int level = oldLevel.toInteger()
                                    int cTemp = oldTemp.toInteger()
                                    if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColorTemp - Reversing Light: ${it} - oldStatus: ${oldStatus} - level: ${level} - cTemp: ${cTemp}"
                                    pauseExecution(actionDelay)
                                    it.setLevel(level)
                                    pauseExecution(actionDelay)
                                    it.setColorTemperature(cTemp)
                                    if(oldStatus == "off") {
                                        if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColorTemp - Turning light off (${it})"
                                        pauseExecution(actionDelay)
                                        it.off()
                                    }
                                } catch(e) {
                                    if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setColorTemp - Turning Off (${it})"
                                    pauseExecution(actionDelay)
                                    it.off()
                                }      
                            } else if(it.hasCommand("setLevel")) {
                                name = (it.displayName).replace(" ","")
                                try {
                                    data = state.oldLevelMapPer.get(name)
                                    def (oldStatus, oldLevel) = data.split("::")
                                    int level = oldLevel.toInteger()
                                    if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setLevel - Reversing Light: ${it} - oldStatus: ${oldStatus} - level: ${level}"
                                    pauseExecution(actionDelay)
                                    it.setLevel(level)
                                    if(oldStatus == "off") {
                                        if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setLevel - Turning light off (${it})"
                                        pauseExecution(actionDelay)
                                        it.off()
                                    }
                                } catch(e) {
                                    if(logEnable) log.debug "In switchesPerModeReverseActionHandler - setLevel - Turning Off (${it})"
                                    pauseExecution(actionDelay)
                                    it.off()
                                }
                            }
                        } else {
                            if(logEnable) log.debug "In switchesPerModeReverseActionHandler - ${it} was already off - Nothing to do"
                        }
                    }
                }
            }
        }
    }
    state.setOldMapPer = false
    state.setOldLevelMapPer = false
}

def switchesInSequenceHandler() {
    if(logEnable) log.debug "In switchesInSequenceHandler (${state.version})"
    if(deviceSeqAction1) {
        deviceSeqAction1.each { it ->
            pauseExecution(actionDelay)
            it.on()
        }
    }
    if(deviceSeqAction2) {
        deviceSeqAction2.each { it ->
            pauseExecution(actionDelay)
            it.on()
        }
    }
    if(deviceSeqAction3) {
        deviceSeqAction3.each { it ->
            pauseExecution(actionDelay)
            it.on()
        }
    }
    if(deviceSeqAction4) {
        deviceSeqAction4.each { it ->
            pauseExecution(actionDelay)
            it.on()
        }
    }
    if(deviceSeqAction5) {
        deviceSeqAction5.each { it ->
            pauseExecution(actionDelay)
            it.on()
        }
    }
}

def switchesInSequenceReverseHandler() {
    if(logEnable) log.debug "In switchesInSequenceReverseHandler (${state.version})"
    if(deviceSeqAction5) {
        deviceSeqAction5.each { it ->
            pauseExecution(actionDelay)
            it.off()
        }
    }
    if(deviceSeqAction4) {
        deviceSeqAction4.each { it ->
            pauseExecution(actionDelay)
            it.off()
        }
    }
    if(deviceSeqAction3) {
        deviceSeqAction3.each { it ->
            pauseExecution(actionDelay)
            it.off()
        }
    }
    if(deviceSeqAction2) {
        deviceSeqAction2.each { it ->
            pauseExecution(actionDelay)
            it.off()
        }
    }
    if(deviceSeqAction1) {
        deviceSeqAction1.each { it ->
            pauseExecution(actionDelay)
            it.off()
        }
    }
}

def dimmerOnActionHandler() {
    if(logEnable) log.debug "In dimmerOnActionHandler (${state.version})"
    state.fromWhere = "dimmerOn"
    state.dimmerDevices = setOnLC
    state.onColor = "${colorLC}"
    state.onLevel = levelLC
    state.onTemp = tempLC
    setLevelandColorHandler()
}

def dimmerOnReverseActionHandler() {
    if(setOnLC) {
        setOnLC.each { it ->
            currentONOFF = it.currentValue("switch")
            if(currentONOFF == "on") {
                if(it.hasCommand("setColor")) {
                    name = (it.displayName).replace(" ","")
                    try {
                        data = state.oldMap.get(name)
                        log.trace "I'm here now - data: ${data}"
                        def (oldStatus, oldHueColor, oldSaturation, oldLevel, oldColorTemp, oldColorMode) = data.split("::")
                        int hueColor = oldHueColor.toInteger()
                        int saturation = oldSaturation.toInteger()
                        int level = oldLevel.toInteger()
                        int cTemp = oldColorTemp.toInteger()
                        def cMode = oldColorMode
                        if(cMode == "CT") {
                            if(logEnable) log.debug "In dimmerOnReverseActionHandler - setColor - Reversing Light: ${it} - oldStatus: ${oldStatus} - cTemp: ${ctemp} - level: ${level}"
                            pauseExecution(actionDelay)
                            it.setColorTemperature(cTemp)
                            pauseExecution(actionDelay)
                            it.setLevel(level)                          
                            if(oldStatus == "off") {                            
                                if(logEnable) log.debug "In dimmerOnReverseActionHandler - setColor - Turning light off (${it})"
                                pauseExecution(actionDelay)
                                it.off()
                            }
                        } else {
                            def theValue = [hue: hueColor, saturation: saturation, level: level]
                            if(logEnable) log.debug "In dimmerOnReverseActionHandler - setColor - Reversing Light: ${it} - oldStatus: ${oldStatus} - theValue: ${theValue}"
                            pauseExecution(actionDelay)
                            it.setColor(theValue)
                            if(oldStatus == "off") {
                                if(logEnable) log.debug "In dimmerOnReverseActionHandler - setColor - Turning light off (${it})"
                                pauseExecution(actionDelay)
                                it.off()
                            }
                        }
                    } catch(e) {
                        if(logEnable) log.debug "In dimmerOnReverseActionHandler - setColor - Turning Off (${it})"
                        pauseExecution(actionDelay)
                        it.off()
                    }
                } else if(it.hasCommand("setColorTemperature")) {
                    name = (it.displayName).replace(" ","")
                    try {
                        data = state.oldLevelMap.get(name)
                        def (oldStatus, oldLevel, oldTemp) = data.split("::")
                        int level = oldLevel.toInteger()
                        int cTemp = oldColorTemp.toInteger()
                        if(logEnable) log.debug "In dimmerOnReverseActionHandler - setColorTemp - Reversing Light: ${it} - oldStatus: ${oldStatus} - level: ${level} - cTemp: ${cTemp}"
                        pauseExecution(actionDelay)
                        it.setLevel(level)
                        pauseExecution(actionDelay)
                        it.setColorTemperature(cTemp)
                        if(oldStatus == "off") {
                            if(logEnable) log.debug "In dimmerOnReverseActionHandler - setColorTemp - Turning light off (${it})"
                            pauseExecution(actionDelay)
                            it.off()
                        }
                    } catch(e) {
                        if(logEnable) log.debug "In dimmerOnReverseActionHandler - setColorTemp - Turning Off (${it})"
                        pauseExecution(actionDelay)
                        it.off()
                    }      
                } else if(it.hasCommand("setLevel")) {
                    name = (it.displayName).replace(" ","")
                    try {
                        data = state.oldLevelMap.get(name)
                        def (oldStatus, oldLevel) = data.split("::")
                        int level = oldLevel.toInteger()
                        if(logEnable) log.debug "In dimmerOnReverseActionHandler - setLevel - Reversing Light: ${it} - oldStatus: ${oldStatus} - level: ${level}"
                        pauseExecution(actionDelay)
                        it.setLevel(level)
                        if(oldStatus == "off") {
                            if(logEnable) log.debug "In dimmerOnReverseActionHandler - setLevel - Turning light off (${it})"
                            pauseExecution(actionDelay)
                            it.off()
                        }
                    } catch(e) {
                        if(logEnable) log.debug "In dimmerOnReverseActionHandler - setLevel - Turning Off (${it})"
                        pauseExecution(actionDelay)
                        it.off()
                    }
                }
            } else {
                if(logEnable) log.debug "In dimmerOnReverseActionHandler - ${it} was already off - Nothing to do"
            }
        }
        state.setOldMap = false
        state.setOldLevelMap = false
    }
}

def permanentDimHandler() {
    if(setDimmersPerMode) {
        currentMode = location.mode
        setDimmersPerMode.each { it ->
            if(logEnable) log.debug "In permanentDimHandler - Working on $it"
            def theData = "${state.sdPerModeMap}".split(",")
            theData.each { itTwo -> 
                def (theMode, theDevice, theLevel, theTemp, theColor) = itTwo.split(":")
                if(theMode.startsWith(" ") || theMode.startsWith("[")) theMode = theMode.substring(1)
                theColor = theColor.replace("]","")           
                def cleandevice = theDevice.replace("[","").replace("]","")
                def cleanOne = "${it}"
                if(cleandevice == cleanOne) {
                    if(currentMode == theMode) {
                        state.fromWhere = "permanentDimPerHandler"
                        state.dimmerDevices = it
                        state.onColor = theColor
                        if(permanentDimLvl) state.onLevel = permanentDimLvl
                        if(warningDimLvl) state.onLevel = warningDimLvl
                        state.onTemp = theTemp
                        setLevelandColorHandler()
                    }
                }
            }
        }
    }
    if(setOnLC) {
        if(logEnable) log.debug "In permanentDimHandler - Set Level Dim - Permanent Dim Level: ${permanentDimLvl} - Waring Dim Level: ${warningDimLvl} - Color: ${pdColor} - Temp: ${pdTemp}"
        state.fromWhere = "permanentDimHandler"
        if(permanentDimLvl) state.onLevel = permanentDimLvl
        if(warningDimLvl) state.onLevel = warningDimLvl
        state.onColor = pdColor
        setLevelandColorHandler()
    }
    if(switchesOnAction) {
        switchesOnAction.each { it ->
            if(it.hasCommand('setLevel')) {
                if(logEnable) log.debug "In permanentDimHandler - Set Level Dim (on) - Permanent Dim Level: ${permanentDimLvl} - Waring Dim Level: ${warningDimLvl}"
                pauseExecution(actionDelay)
                if(permanentDimLvl) it.setLevel(permanentDimLvl)
                if(warningDimLvl) it.setLevel(warningDimLvl)
            }
        }
    }
    if(switchesOffAction) {
        switchesOffAction.each { it ->
            if(it.hasCommand('setLevel')) {
                if(logEnable) log.debug "In permanentDimHandler - Set Level Dim (off) - Permanent Dim Level2: ${permanentDimLvl2} - Waring Dim Level: ${warningDimLvl}"
                pauseExecution(actionDelay)
                if(permanentDimLvl2) it.setLevel(permanentDimLvl2)
                if(warningDimLvl) it.setLevel(warningDimLvl)
            }
        }
    }
}

def devicesToRefreshActionHandler() {
    devicesToRefresh.each { it ->
        if(logEnable) log.debug "In devicesToRefreshActionHandler - Refreshing ${it}"
        pauseExecution(actionDelay)
        it.refresh()
    }
}

def fanActionHandler() {
    fanAction.each { it ->
        if(logEnable) log.debug "In fanActionHandler - Changing ${it} to ${fanSpeed}"
        if(state.setFanOldMap == false) {
            state.oldFanMap = [:]
            name = (it.displayName).replace(" ","")
            status = it.currentValue("speed")
            oldStatus = "${status}"
            state.oldFanMap.put(name,oldStatus) 
            state.setFanOldMap = true
        }
        log.warn "oldStatus: ${oldStatus}"
        pauseExecution(actionDelay)
        it.setSpeed(fanSpeed)
    }
}

def fanReverseActionHandler() {
    if(state.oldFanMap) {
        fanAction.each { it ->
            name = (it.displayName).replace(" ","")
            data = state.oldFanMap.get(name)
            def fanSpeed = data
            if(logEnable) log.debug "In fanReverseActionHandler - Changing ${it} to ${fanSpeed}"
            pauseExecution(actionDelay)
            it.setSpeed(fanSpeed)
            state.setFanOldMap = false
        }
    }
}

def garageDoorActionHandler() {
    if(logEnable) log.debug "In garageDoorActionHandler (${state.version})"
    if(garageDoorClosedAction) {
        garageDoorClosedAction.each { it ->
            if(logEnable) log.debug "In garageDoorActionHandler - Closing ${it}"
            pauseExecution(actionDelay)
            it.close()
        }
    }
    if(garageDoorOpenAction) {
        garageDoorOpenAction.each { it ->
            if(logEnable) log.debug "In garageDoorActionHandler - Open ${it}"
            pauseExecution(actionDelay)
            it.open()
        }
    }
}

def hsmChangeActionHandler() {
    if(logEnable) log.debug "In hsmChangeActionHandler (${state.version}) - Setting to ${setHSM}"
    pauseExecution(actionDelay)
    sendLocationEvent (name: "hsmSetArm", value: "${setHSM}")
}

def lockActionHandler() {
    if(logEnable) log.debug "In lockActionHandler (${state.version})"
    if(lockAction) {
        lockAction.each { it ->
            if(logEnable) log.debug "In lockActionHandler - Locking ${it}"
            pauseExecution(actionDelay)
            it.lock()
        }
    }
    if(unlockAction) {
        unlockAction.each { it ->
            if(logEnable) log.debug "In unlockActionHandler - Unlocking ${it}"
            pauseExecution(actionDelay)
            it.unlock()
        }
    }
}

def lockUserActionHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.warn "In lockUserActionHandler (${state.version})"
        if(evt) {
            lockdata = evt.data
            lockStatus = evt.value
            lockName = evt.displayName
            if(logEnable) log.debug "In lockUserActionHandler (${state.version}) - Lock: ${lockName} - Status: ${lockStatus}"
            if(lockStatus == "unlocked") {
                if(logEnable) log.debug "In lockUserActionHandler - Lock: ${lockName} - Status: ${lockStatus} - We're in!"
                if(theLocks) {
                    if (lockdata && !lockdata[0].startsWith("{")) {
                        lockdata = decrypt(lockdata)
                        if (lockdata == null) {
                            log.debug "Unable to decrypt lock code from device: ${lockName}"
                            return
                        }
                    }
                    def codeMap = parseJson(lockdata ?: "{}").find{ it }
                    if (!codeMap) {
                        if(logEnable) log.debug "In lockUserActionHandler - Lock Code not available."
                        return
                    }
                    codeName = "${codeMap?.value?.name}"
                    if(logEnable) log.debug "In lockUserActionHandler - ${lockName} was unlocked by ${codeName}"	
                }
            }
        }
    }
}

def modeChangeActionHandler() {
    if(logEnable) log.debug "In modeChangeActionHandler - Changing mode to ${modeAction}"
    pauseExecution(actionDelay)
    setLocationMode(modeAction)
}

def sendHTTPActionHandler(String varCommand, String method) {
    if(logEnable) log.debug "In sendHTTPActionHandler - Sending GET to URL: ${httpIP}:${httpPort}${varCommand}"
    
    httpPost(
        [
            uri: "http://${httpIP}:${httpPort}",
            path: varCommand,
            headers:
            [
                "Cookie": cookie
            ]
        ]
    ) 
    {
        resp ->
    }  
}

def runCmd(String varCommand, String method) {        // Based on code from Dan Ogorchock - @ogiewon - Thank you
    if(logEnable) log.debug "In runCmd (${state.version})"
    def path = varCommand 
    def body = ""
    if(httpBody) body = httpBody
    def headers = [:] 
    headers.put("HOST", "${httpIP}:${httpPort}")
    headers.put("Content-Type", httpContent)
    try {
        def hubAction = new hubitat.device.HubAction(
            method: method,
            path: path,
            body: body,
            headers: headers
            )
        if(logEnable) log.debug "In runCmd - Action: $hubAction"
        return hubAction
    }
    catch (Exception e) {
        log.warn "In runCmd - runCmd hit exception ${e} on ${hubAction}"
    }  
}

// ***** Start Cog Copy *****
def requestCogCopy() {             // this is sent to the parent app
    if(testLogEnable) log.info "In requestCogCopy (${state.version})"
    toCog = app.id
    parent.getCogSettings(fromCog,toCog)
}

def sendChildSettings() {           // this is then requested from the parent app
    if(testLogEnable) log.info "In sendChildSettings (${state.version})"   
    childAppSettings = settings
}

def doTheCogCopy(newSettings) {    // and finally the parent app sends the settings!
    if(testLogEnable) log.info "In doTheCogCopy (${state.version})"
    if(newSettings) {
        if(testLogEnable) log.info "In doTheCogCopy - Received: ${newSettings}"
        
        newSettings.each { theOption ->
            name = theOption.key
            nameValue = theOption.value
            
            nValue = nameValue.toString()
            if(nValue && (name != "cloneCog" && name != "copyCog" && name != "fromCog")) {
                if(testLogEnable) log.info "In doTheCogCopy - Working on $name - value: $nameValue"
                if(nValue.contains("[")) {
                    app.updateSetting(name,[type:"enum",value:nameValue])
                } else {
                    app.updateSetting(name,nameValue)
                }

                //app.updateSetting(name,nameValue)
            }     
        }
    }
    if(testLogEnable) log.info "In doTheCogCopy - Finished"
    app.updateSetting("copyCog",[value:"false",type:"bool"])
    app.updateSetting("fromCog",[value:"",type:"number"])
    app.updateSetting("cloneCog",[value:"false",type:"bool"])
    if(testLogEnable) log.info "In doTheCogCopy - New settings: $settings"
    state.copyFinished = true
}
// ***** End Cog Copy *****

def switchesOnActionHandler() {
    switchesOnAction.each { it ->
        if(logEnable) log.debug "In switchesOnActionHandler - Turning on ${it}"
        pauseExecution(actionDelay)
        it.on()
    }
}

def switchesOnReverseActionHandler() {
    switchesOnAction.each { it ->
        if(logEnable) log.debug "In switchesOnReverseActionHandler - Turning off ${it}"
        pauseExecution(actionDelay)
        it.off()
    }
}

def switchesOffActionHandler() {
    switchesOffAction.each { it ->
        if(logEnable) log.debug "In switchesOffActionHandler - Turning off ${it}"
        pauseExecution(actionDelay)
        it.off()
    }
}

def switchesOffReverseActionHandler() {
    switchesOffAction.each { it ->
        if(logEnable) log.debug "In switchesOffReverseActionHandler - Turning on ${it}"
        pauseExecution(actionDelay)
        it.on()
    }
}

def switchesToggleActionHandler() {
    switchesToggleAction.each { it ->
        status = it.currentValue("switch")
        if(status == "off") {
            if(logEnable) log.debug "In switchesToggleActionHandler - Turning on ${it}"
            pauseExecution(actionDelay)
            it.on()
        } else {
            if(logEnable) log.debug "In switchesToggleActionHandler - Turning off ${it}"
            pauseExecution(actionDelay)
            it.off()
        }
    }
}

def slowOnHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In slowOnHandler (${state.version})"
        state.fromWhere = "slowOn"
        state.currentLevel = startLevelHigh ?: 1
        state.onLevel = state.currentLevel
        state.onColor = "${colorUp}"
        setLevelandColorHandler()
        if(minutesUp == 0) return
        seconds = (minutesUp * 60) - 10
        difference = targetLevelHigh - state.currentLevel
        state.dimStep = (difference / seconds) * 10
        if(logEnable) log.debug "In slowOnHandler - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh} - color: ${state.onColor}"
        atLeastOneUpOn = false
        runIn(5,dimStepUp)
    }
}

def slowOffHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In slowOffHandler (${state.version})"
        state.fromWhere = "slowOff"
        if(useMaxLevel) {
            findHighestCurrentValue()
        } else {
            state.highestLevel = startLevelLow ?: 99
        }
        state.onColor = "${colorDn}"
        setLevelandColorHandler()
        if(minutesDn == 0) return
        seconds = (minutesDn * 60) - 10
        difference = state.highestLevel - targetLevelLow
        state.dimStep1 = (difference / seconds) * 10
        if(logEnable) log.debug "slowOffHandler - highestLevel: ${state.highestLevel} - targetLevel: ${targetLevelLow} - dimStep1: ${state.dimStep1} - color: ${state.onColor}"
        atLeastOneDnOn = false
        runIn(5,dimStepDown)
    }
}

def findHighestCurrentValue() {
    if(logEnable) log.debug "In findHighestCurrentValue (${state.version})"
    state.highestLevel = 0
    slowDimmerDn.each { it->
        checkLevel = it.currentValue("level")
        if(checkLevel > state.highestLevel) state.highestLevel = checkLevel
    }
    if(logEnable) log.debug "In findHighestCurrentValue - highestLevel: ${state.highestLevel})"
}

def dimStepUp() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable && logSize) log.debug "-------------------- dimStepUp --------------------"
        if(logEnable && logSize) log.debug "In dimStepUp (${state.version})"
        if(state.currentLevel < targetLevelHigh) {
            state.currentLevel = state.currentLevel + state.dimStep
            if(state.currentLevel > targetLevelHigh) { state.currentLevel = targetLevelHigh }
            if(logEnable && logSize) log.debug "In dimStepUp - Setting currentLevel: ${state.currentLevel} - dimStep: ${state.dimStep} - targetLevel: ${targetLevelHigh}"
            slowDimmerUp.each { it->
                deviceOn = it.currentValue("switch")
                if(logEnable && logSize) log.debug "In dimStepUp - ${it} is: ${deviceOn}"
                if(deviceOn == "on") {
                    atLeastOneUpOn = true
                    it.setLevel(state.currentLevel)
                }
            }
            if(atLeastOneUpOn) {
                runIn(10,dimStepUp)
            } else {
                log.info "${app.label} - All devices are turned off"
            }
        } else {
            if(logEnable && logSize) log.debug "-------------------- End dimStepUp --------------------"
            if(logEnable) log.info "In dimStepUp - Current Level: ${state.currentLevel} has reached targetLevel: ${targetLevelHigh}"
        }
    }
}

def dimStepDown() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable && logSize) log.debug "-------------------- dimStepDown --------------------"
        if(logEnable && logSize) log.debug "In dimStepDown (${state.version})"
        if(state.highestLevel > targetLevelLow) {
            state.highestLevel = state.highestLevel - state.dimStep1
            if(state.highestLevel < targetLevelLow) { state.highestLevel = targetLevelLow }
            if(logEnable && logSize) log.debug "In dimStepDown - Starting Level: ${state.highestLevel} - targetLevelLow: ${targetLevelLow}"
            slowDimmerDn.each { it->
                deviceOn = it.currentValue("switch")
                int cLevel = it.currentValue("level")
                int wLevel = state.highestLevel
                if(logEnable && logSize) log.debug "In dimStepDown - ${it} is: ${deviceOn} - cLevel: ${cLevel} - wLevel: ${wLevel}"
                if(deviceOn == "on") {
                    atLeastOneDnOn = true
                    if(wLevel <= cLevel) { it.setLevel(wLevel) }
                }
            }
            if(atLeastOneDnOn) {
                runIn(10,dimStepDown)
            } else {
                log.info "${app.label} - All devices are turned off"
            }
        } else {
            if(dimDnOff) slowDimmerDn.off()
            if(logEnable && logSize) log.debug "-------------------- End dimStepDown --------------------"
            if(logEnable) log.info "In dimStepDown - Current Level: ${state.currentLevel} has reached targetLevel: ${targetLevelLow}"
        } 
    }
}

def thermostatActionHandler() {
    thermostatAction.each { it ->
        if(setThermostatFanMode) {
            if(logEnable) log.debug "In thermostatActionHandler - Fan Mode - Setting ${it} to ${setThermostatFanMode}"
            pauseExecution(actionDelay)
            it.setThermostatFanMode(setThermostatFanMode)
        }
        if(setThermostatMode) {
            if(logEnable) log.debug "In thermostatActionHandler - Thermostat Mode - Setting ${it} to ${setThermostatMode}"
            pauseExecution(actionDelay)
            it.setThermostatMode(setThermostatMode)
        }
        if(coolingSetpoint) {
            if(logEnable) log.debug "In thermostatActionHandler - Cooling Setpoint - Setting ${it} to ${coolingSetpoint}"
            pauseExecution(actionDelay)
            it.setCoolingSetpoint(coolingSetpoint)
        }
        if(heatingSetpoint) {
            if(logEnable) log.debug "In thermostatActionHandler - Heating Setpoint - Setting ${it} to ${heatingSetpoint}"
            pauseExecution(actionDelay)
            it.setHeatingSetpoint(heatingSetpoint)
        }
    }
}

def valveActionHandler() {
    if(logEnable) log.debug "In valveActionHandler (${state.version})"
    if(valveClosedAction) {
        valveClosedAction.each { it ->
            if(logEnable) log.debug "In valveActionHandler - Closing ${it}"
            pauseExecution(actionDelay)
            it.close()
        }
    }
    if(valveOpenAction) {
        valveOpenAction.each { it ->
            if(logEnable) log.debug "In valveActionHandler - Open ${it}"
            pauseExecution(actionDelay)
            it.open()
        }
    }
}

def contactActionHandler() {
    if(logEnable) log.debug "In contactActionHandler (${state.version})"
    if(contactClosedAction) {
        contactClosedAction.each { it ->
            if(logEnable) log.debug "In contactActionHandler - Closing ${it}"
            pauseExecution(actionDelay)
            it.close()
        }
    }
    if(contactOpenAction) {
        contactOpenAction.each { it ->
            if(logEnable) log.debug "In contactActionHandler - Open ${it}"
            pauseExecution(actionDelay)
            it.open()
        }
    }
}

def contactReverseActionHandler() {
    if(logEnable) log.debug "In contactReverseActionHandler (${state.version})"
    if(contactClosedAction) {
        contactClosedAction.each { it ->
            if(logEnable) log.debug "In contactReverseActionHandler - Open ${it}"
            pauseExecution(actionDelay)
            it.open()
        }
    }
    if(contactOpenAction) {
        contactOpenAction.each { it ->
            if(logEnable) log.debug "In contactReverseActionHandler - Close ${it}"
            pauseExecution(actionDelay)
            it.close()
        }
    }
}
// ********** End Actions **********

def messageHandler() {
    if(logEnable) log.debug "In messageHandler (${state.version}) - doMessage: ${state.doMessage}"
    if(msgRepeatContact) { subscribe(msgRepeatContact, "contact", repeatCheck) }    
    if(msgRepeatSwitch) { subscribe(msgRepeatSwitch, "switch", repeatCheck) }

    if(msgRepeat) {
        state.msgRepMax = msgRepeatMax ?: 2
        if(state.repeatCount == null) state.repeatCount = 0
        if(logEnable) log.debug "In messageHandler (${state.version}) - repeatCount: ${state.repeatCount} - msgRepeatMax: ${state.msgRepMax}"
        if(state.repeatCount > state.msgRepMax) { state.doMessage = false }
        state.repeatCount = state.repeatCount + 1
    }
    if(state.doMessage) {   
        if(triggerType) {
            if(triggerType.contains("xBattery") || triggerType.contains("xEnergy") || triggerType.contains("xHumidity") || triggerType.contains("xIlluminance") || triggerType.contains("xPower") || triggerType.contains("xTemp")) {
                if(logEnable && logSize) log.debug "In messageHandler (setpoint) - setpointHighOK: ${state.setpointHighOK} - setpointLowOK: ${state.setpointLowOK}"
                if(state.setpointHighOK == "no") theMessage = "${messageH}"
                if(state.setpointLowOK == "no") theMessage = "${messageL}"
            } else {
                theMessage = message
            }
            if(logEnable && logSize) log.debug "In messageHandler - Random - raw message: ${theMessage}"
            def values = "${theMessage}".split(";")
            vSize = values.size()
            count = vSize.toInteger()
            def randomKey = new Random().nextInt(count)
            msg1 = values[randomKey]
            if(logEnable && logSize) log.debug "In messageHandler - Random - msg1: ${msg1}" 
        }
        state.message = msg1
        if(state.message) { 
            if (state.message.contains("%whatHappened%")) {state.message = state.message.replace('%whatHappened%', state.whatHappened)}
            if (state.message.contains("%whoHappened%")) {state.message = state.message.replace('%whoHappened%', state.whoHappened)}
            if (state.message.contains("%whoUnlocked%")) {state.message = state.message.replace('%whoUnlocked%', state.whoUnlocked)}
            if (state.message.contains("%time%")) {
                currentDateTime()
                state.message = state.message.replace('%time%', state.theTime)
            }
            if (state.message.contains("%time1%")) {
                currentDateTime()
                state.message = state.message.replace('%time1%', state.theTime1)
            }
            if(logEnable) log.debug "In messageHandler - message: ${state.message}"
            if(state.message && state.message != "null") {
                if(useSpeech) letsTalk(state.message)
                if(sendPushMessage) pushHandler(state.message)
            }
        }
        if(msgRepeat) {
            repeatSeconds = msgRepeatMinutes * 60
            runIn(repeatSeconds, messageHandler)
        }
    } else {
        if(logEnable) log.debug "In messageHandler - Repeat is now off"
        unsubscribe(msgRepeatContact)
        unsubscribe(msgRepeatSwitch)
        state.repeatCount = 0
        state.doMessage = false
    }
}

def repeatCheck(evt) {
    if(logEnable) log.debug "In repeatCheck (${state.version}) - Repeat Check was triggered, Repeat Off"
    unsubscribe(msgRepeatContact)
    unsubscribe(msgRepeatSwitch)
    state.repeatCount = 0
    state.doMessage = false
}

def letsTalk(msg) {
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}"
    if(useSpeech && fmSpeaker) {
        fmSpeaker.latestMessageFrom(state.name)
        fmSpeaker.speak(msg)
    }
}

def pushHandler(msg){
    if(logEnable) log.debug "In pushNow (${state.version}) - Sending a push - msg: ${msg}"
    theMessage = "${app.label} - ${msg}"
    if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}"
    sendPushMessage.deviceNotification(theMessage)
}

def theFlasherHandler() {
    if(logEnable) log.debug "In theFlasherHandler (${state.version})"
    flashData = "Preset::${flashOnTriggerPreset}"
    if(logEnable) log.debug "In theFlasherHandler - Sending: ${flashData}"
    theFlasherDevice.sendPreset(flashData)    
}

def currentDateTime() {
    if(logEnable && logSize) log.debug "In currentDateTime (${state.version})"
    Date date = new Date()
    String datePart = date.format("dd/MM/yyyy")
    String timePart = date.format("HH:mm")
    String timePart1 = date.format("h:mm a")
    state.theTime = timePart		// 24 h
    state.theTime1 = timePart1		// AM PM
    if(logEnable) log.debug "In currentDateTime - ${state.theTime}"
}

// *****  Start Time Handlers *****
def autoSunHandler() {
    if(logEnable) log.debug "In autoSunHandler (${state.version})"
    if(triggerType.contains("tTimeDays")) {
        if(timeDaysType.contains("tSunsetSunrise") || timeDaysType.contains("tSunrise") || timeDaysType.contains("tSunset")) {
            def riseAndSet = getSunriseAndSunset()
            def sunriseTime = (riseAndSet.sunrise)+1
            def sunsetTime = riseAndSet.sunset
            int theOffsetSunset = offsetSunset ?: 1
            if(setBeforeAfter) {
                state.timeSunset = new Date(sunsetTime.time + (theOffsetSunset * 60 * 1000))
            } else {
                state.timeSunset = new Date(sunsetTime.time - (theOffsetSunset * 60 * 1000))
            }
            int theOffsetSunrise = offsetSunrise ?: 1
            if(riseBeforeAfter) {
                state.timeSunrise = new Date(sunriseTime.time + (theOffsetSunrise * 60 * 1000))
            } else {
                state.timeSunrise = new Date(sunriseTime.time - (theOffsetSunrise * 60 * 1000))
            }
            if(logEnable && logSize) log.debug "In autoSunHandler - sunsetTime: ${sunsetTime} - theOffsetSunset: ${theOffsetSunset} - setBeforeAfter: ${setBeforeAfter}"
            if(logEnable && logSize) log.debug "In autoSunHandler - sunriseTime: ${sunriseTime} - theOffsetSunrise: ${theOffsetSunrise} - riseBeforeAfter: ${riseBeforeAfter}"
            if(logEnable) log.debug "In autoSunHandler - timeSunset: ${state.timeSunset} - timeSunrise: ${state.timeSunrise}"
            schedule("0 5 12 ? * * *", autoSunHandler)
            schedule(state.timeSunset, runAtTime1)
            schedule(state.timeSunrise, runAtTime2)
        }
    }
    if(sunriseEndTime) schedule(sunriseEndTime, runAtTime2)
    if(sunsetEndTime) schedule(sunsetEndTime, runAtTime2)
}

def runAtTime1() {
    if(logEnable) log.debug "In runAtTime1 (${state.version}) - Starting"
    startTheProcess("run")
}

def runAtTime2() {
    if(logEnable) log.debug "In runAtTime2 (${state.version}) - Starting"
    startTheProcess("reverse")
}

def checkTimeSun() {
    if(logEnable) log.debug "In checkTimeSun (${state.version})"
    if(triggerType.contains("tTimeDays")) {
        if(timeDaysType.contains("tSunsetSunrise") || timeDaysType.contains("tSunrise") || timeDaysType.contains("tSunset")) {
            def riseAndSet = getSunriseAndSunset()
            def nextSunrise = (riseAndSet.sunrise)+1
            def nextSunset = riseAndSet.sunset
            int theOffsetSunset = offsetSunset ?: 1
            if(setBeforeAfter) {
                use( TimeCategory ) { nextSunsetOffset = nextSunset + theOffsetSunset.minutes }
            } else {
                use( TimeCategory ) { nextSunsetOffset = nextSunset - theOffsetSunset.minutes }
            }
            int theOffsetSunrise = offsetSunrise ?: 1
            if(riseBeforeAfter) {
                use( TimeCategory ) { nextSunriseOffset = nextSunrise + theOffsetSunrise.minutes }
            } else {
                use( TimeCategory ) { nextSunriseOffset = nextSunrise - theOffsetSunrise.minutes }
            } 
            //if(logEnable) log.debug "In checkTimeSun - nextSunset: ${nextSunset} - nextSunsetOffset: ${nextSunsetOffset}"
            //if(logEnable) log.debug "In checkTimeSun - nextSunrise: ${nextSunrise} - nextSunriseOffset: ${nextSunriseOffset}"
            state.timeBetweenSun = timeOfDayIsBetween(nextSunsetOffset, nextSunrise, new Date(), location.timeZone)
            if(logEnable) log.debug "In checkTimeSun - nextSunsetOffset: ${nextSunsetOffset} - nextSunriseOffset: ${nextSunriseOffset}"

            if(state.timeBetweenSun) {
                if(logEnable) log.debug "In checkTimeSun - Time within range"
            } else {
                if(logEnable) log.debug "In checkTimeSun - Time outside of range"
            }
        } else {
            state.timeBetweenSun = true
        }
    } else {
        state.timeBetweenSun = true
    }
    if(logEnable) log.debug "In checkTimeSun - timeBetweenSun: ${state.timeBetweenSun}"
}

def checkTimeBetween() {
    if(logEnable) log.debug "In checkTimeBetween (${state.version})"
    if(fromTime && toTime) {
        startTime = toDateTime(fromTime)
        if(midnightCheckR) {
            endTime = toDateTime(toTime)+1
        } else {
            endTime = toDateTime(toTime)
        }
        state.betweenTime = timeOfDayIsBetween(startTime, endTime, new Date(), location.timeZone)
        if(logEnable) log.debug "In checkTimeBetween - ${startTime} - ${endTime}"
        if(state.betweenTime) {
            if(logEnable) log.debug "In checkTimeBetween - Time within range"

        } else {
            if(logEnable) log.debug "In checkTimeBetween - Time outside of range"
        }
    } else {
        if(logEnable) log.debug "In checkTimeBetween - NO Time Restriction Specified"
        state.betweenTime = true
    }    
    if(fromTime) { schedule(fromTime, runAtTime1) }
    if(toTime) { schedule(toTime, runAtTime2) }
    if(logEnable) log.debug "In checkTimeBetween - betweenTime: ${state.betweenTime}"
}

def certainTime() {
    if(logEnable) log.debug "In certainTime (${state.version})"  
    startTheProcess()
}

def dayOfTheWeekHandler() {
    if(logEnable) log.debug "In dayOfTheWeek (${state.version})"
    if(days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        df.setTimeZone(location.timeZone)
        def day = df.format(new Date())
        def dayCheck = days.contains(day)
        if(dayCheck) {
            state.daysMatch = true
        } else {
            state.daysMatch = false
        }
    } else {
        state.daysMatch = true
    }
    if(logEnable) log.debug "In dayOfTheWeekHandler - daysMatch: ${state.daysMatch}"
}

def modeHandler() {
    if(logEnable) log.debug "In modeHandler (${state.version})"
    if(modeEvent) {
        theValue = location.mode
        def modeCheck = modeEvent.contains(theValue)
        if(modeCheck) {
            state.modeMatch = true
        } else {
            state.modeMatch = false
        }
    } else {
        state.modeMatch = true
    }
    if(logEnable) log.debug "In modeHandler - modeMatch: ${state.modeMatch}"
}
// *****  End Time Handlers *****

def checkingWhatToDo() {
    if(logEnable) log.debug "In checkingWhatToDo (${state.version})"    
    if(state.betweenTime && state.timeBetweenSun && state.modeMatch && state.daysMatch) {
        state.timeOK = true
    } else {
        state.timeOK = false
    }
    if(triggerAndOr) {
        if(logEnable) log.debug "In checkingWhatToDo - USING OR - totalMatch: ${state.totalMatch} - totalMatchHelper: ${state.totalMatchHelper} - setpointOK: ${state.setpointOK} - timeOK: ${state.timeOK}"
        if(state.timeOK) {
            if((state.totalMatch >= 1) || state.setpointOK) {
                state.everythingOK = true
            } else {
                if(state.totalMatchHelper >= 1) {
                    state.everythingOK = true
                } else {
                    state.everythingOK = false
                }
            }
        } else {
            state.everythingOK = false
        }
    } else {
        if(logEnable) log.debug "In checkingWhatToDo - USING AND - totalMatch: ${state.totalMatch} - totalMatchHelper: ${state.totalMatchHelper} - totalConditions: ${state.totalConditions} - setpointOK: ${state.setpointOK} - timeOK: ${state.timeOK}"
        if((state.totalMatch == state.totalConditions) && state.setpointOK && state.timeOK) {
            state.everythingOK = true
        } else {
            if(state.totalMatchHelper >= 1) {
                state.everythingOK = true
            } else {
                state.everythingOK = false
            }
        }
    }   
    if(logEnable) log.debug "In checkingWhatToDo - everythingOK: ${state.everythingOK}"
    if(state.everythingOK) {
            state.whatToDo = "run"
            if(logEnable) log.debug "In checkingWhatToDo - Using A - Run"
    } else {
        if(reverse || reverseWithDelay || reverseWhenHigh || reverseWhenLow || reverseWhenBetween) {
            state.whatToDo = "reverse"
            if(logEnable) log.debug "In checkingWhatToDo - Using B - Reverse"
        } else {
            state.whatToDo = "stop"
            if(logEnable) log.debug "In checkingWhatToDo - Using C - Stop"
        }
    }   
    if(logEnable) log.debug "In checkingWhatToDo - **********  whatToDo: ${state.whatToDo}  **********"
}

def setLevelandColorHandler() {
    if(state.setOldMap == null) state.setOldMap = false
    if(state.setOldMapPer == null) state.setOldMapPer = false
    if(state.setOldLevelMap == null) state.setOldLevelMap = false
    if(state.setOldLevelMapPer == null) state.setOldLevelMapPer = false
    if(state.fromWhere == "slowOff") {
        state.onLevel = state.highestLevel
    } else {
        state.onLevel = state.onLevel ?: 99
    }   
    if(state.onColor == null || state.onColor == "null" || state.onColor == "") state.onColor = "No Change"
    if(logEnable) log.debug "In setLevelandColorHandler - fromWhere: ${state.fromWhere}, color: ${state.onColor} - onLevel: ${state.onLevel}"
    switch(state.onColor) {
        case "No Change":
        hueColor = null
        saturation = null
        break;
        case "White":
        hueColor = 52
        saturation = 19
        break;
        case "Daylight":
        hueColor = 53
        saturation = 91
        break;
        case "Soft White":
        hueColor = 23
        saturation = 56
        break;
        case "Warm White":
        hueColor = 20
        saturation = 80
        break;
        case "Blue":
        hueColor = 70
        saturation = 100
        break;
        case "Green":
        hueColor = 39
        saturation = 100
        break;
        case "Yellow":
        hueColor = 25
        saturation = 100
        break;
        case "Orange":
        hueColor = 10
        saturation = 100
        break;
        case "Purple":
        hueColor = 75
        saturation = 100
        break;
        case "Pink":
        hueColor = 83
        saturation = 100
        break;
        case "Red":
        hueColor = 100
        saturation = 100
        break;
    }
    onLevel = state.onLevel.toInteger()
    if(logEnable) log.debug "In setLevelandColorHandler - 1 - hue: ${hueColor} - saturation: ${saturation} - onLevel: ${onLevel}"

    if(state.fromWhere == "switchesPerMode") {
        theDevice = state.sPDM
        if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - switchesPerMode - Working on: ${theDevice}"
        def value = [hue: hueColor, saturation: saturation, level: onLevel]
        if(theDevice.hasCommand('setColor') && state.onTemp == "NA" && state.onColor != "No Change") {
            if(state.setOldMapPer == false) {
                state.oldMapPer = [:]
                oldHueColor = theDevice.currentValue("hue")
                oldSaturation = theDevice.currentValue("saturation")
                oldLevel = theDevice.currentValue("level")
                oldColorTemp = theDevice.currentValue("colorTemperature")
                oldColorMode = theDevice.currentValue("colorMode")
                name = (theDevice.displayName).replace(" ","")
                status = theDevice.currentValue("switch")
                oldStatus = "${status}::${oldHueColor}::${oldSaturation}::${oldLevel}::${oldColorTemp}::${oldColorMode}"
                state.oldMapPer.put(name,oldStatus) 
                state.setOldMapPer = true
                if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - setColor - OLD STATUS - oldStatus: ${name} - ${oldStatus}"
            }            
            if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - setColor - $theDevice.displayName, setColor: $value"
            pauseExecution(actionDelay)
            theDevice.setColor(value)
        } else if(theDevice.hasCommand('setColorTemperature') && state.onColor == "NA" && state.onColor != "No Change") {
            if(state.setOldMapPer == false) {
                state.oldMapPer = [:]
                oldLevel = theDevice.currentValue("level")
                oldColorTemp = theDevice.currentValue("colorTemperature")
                name = (theDevice.displayName).replace(" ","")
                status = theDevice.currentValue("switch")
                oldStatus = "${status}::${oldLevel}::${oldColorTemp}"
                state.oldMapPer.put(name,oldStatus)
                state.setOldMapPer = true
            }
            if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - setColorTemp - $theDevice.displayName, setColorTemp($state.onTemp)"
            pauseExecution(actionDelay)
            theDevice.setLevel(onLevel as Integer ?: 99)
            pauseExecution(actionDelay)
            theDevice.setColorTemperature(state.onTemp)
        } else if(theDevice.hasCommand('setLevel')) {
            if(state.setOldLevelMapPer == false) {
                state.oldLevelMapPer = [:]
                oldLevel = theDevice.currentValue("level")
                name = (theDevice.displayName).replace(" ","")
                status = theDevice.currentValue("switch")
                oldStatus = "${status}::${oldLevel}"
                state.oldLevelMapPer.put(name,oldStatus)
                state.setOldLevelMapPer = true
                if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - setLevel - OLD STATUS - oldStatus: ${name} - ${oldStatus}"
            }
            if(logEnable && logSize) log.debug "In setLevelandColorHandler - switchesPerMode - setLevel - $it.displayName, setLevel: $value"
            pauseExecution(actionDelay)
            theDevice.setLevel(onLevel as Integer ?: 99)
        } else {
            if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - $theDevice.displayName, on()"
            pauseExecution(actionDelay)
            theDevice.on()
        }
    }
    
    if(state.fromWhere == "dimmerOn") {
        if(logEnable) log.debug "In setLevelandColorHandler - dimmerOn/switchesPerMode"
        state.dimmerDevices.each {
            if(logEnable) log.debug "In setLevelandColorHandler - Working on ${state.dimmerDevices}"
            def value = [hue: hueColor, saturation: saturation, level: onLevel] 
            if(logEnable && logSize) log.debug "In setLevelandColorHandler - 2 - hue: ${hueColor} - saturation: ${saturation} - onLevel: ${onLevel} - setOldMap: ${state.setOldMap}"
            if(it.hasCommand('setColor') && state.onColor != "No Change") {
                if(state.setOldMap == false) {
                    state.oldMap = [:]
                    oldHueColor = it.currentValue("hue")
                    oldSaturation = it.currentValue("saturation")
                    oldLevel = it.currentValue("level")
                    oldColorTemp = it.currentValue("colorTemperature")
                    oldColorMode = it.currentValue("colorMode")
                    name = (it.displayName).replace(" ","")
                    status = it.currentValue("switch")
                    oldStatus = "${status}::${oldHueColor}::${oldSaturation}::${oldLevel}::${oldColorTemp}::${oldColorMode}"
                    state.oldMap.put(name,oldStatus) 
                    state.setOldMap = true
                    if(logEnable) log.debug "In setLevelandColorHandler - setColor - OLD STATUS - oldStatus: ${name} - ${oldStatus}"
                }
                if(logEnable) log.debug "In setLevelandColorHandler - setColor - $it.displayName, setColor: $value"
                pauseExecution(actionDelay)
                it.setColor(value)
            } else if(it.hasCommand('setColorTemperature') && state.onColor != "No Change") {
                if(state.setOldMap == false) {
                    state.oldMap = [:]
                    oldLevel = it.currentValue("level")
                    oldColorTemp = it.currentValue("colorTemperature")
                    name = (it.displayName).replace(" ","")
                    status = it.currentValue("switch")
                    oldStatus = "${status}::${oldLevel}::${oldColorTemp}"
                    state.oldMap.put(name,oldStatus)
                    state.setOldMap = true
                }
                if(logEnable) log.debug "In setLevelandColorHandler - setColorTemp - $it.displayName, setColorTemp($state.onTemp)"
                pauseExecution(actionDelay)
                it.setLevel(onLevel as Integer ?: 99)
                pauseExecution(actionDelay)
                it.setColorTemperature(state.onTemp)
            } else if (it.hasCommand('setLevel')) {
                if(state.setOldLevelMap == false) {
                    state.oldLevelMap = [:]
                    oldLevel = it.currentValue("level")
                    name = (it.displayName).replace(" ","")
                    status = it.currentValue("switch")
                    oldStatus = "${status}::${oldLevel}"
                    state.oldLevelMap.put(name,oldStatus)
                    state.setOldLevelMap = true
                    if(logEnable && logSize) log.debug "In setLevelandColorHandler - setLevel - OLD STATUS - oldStatus: ${name} - ${oldStatus}"
                }
                if(logEnable && logSize) log.debug "In setLevelandColorHandler - setLevel - $it.displayName, setLevel: $value"
                pauseExecution(actionDelay)
                it.setLevel(onLevel as Integer ?: 99)
            } else {
                if(logEnable && logSize) log.debug "In setLevelandColorHandler - $it.displayName, on()"
                pauseExecution(actionDelay)
                it.on()
            }
        }
    }

    if(state.fromWhere == "slowOn") {
        slowDimmerUp.each {
            if (it.hasCommand('setColor')) {
                if(logEnable && logSize) log.debug "In setLevelandColorHandler - $it.displayName, setColor: $value"
                it.setColor(value)
            } else if (it.hasCommand('setLevel')) {
                if(logEnable && logSize) log.debug "In setLevelandColorHandler - $it.displayName, setLevel: $value"
                it.setLevel(onLevel as Integer ?: 99)
            } else {
                if(logEnable && logSize) log.debug "In setLevelandColorHandler - $it.displayName, on()"
                it.on()
            }
        }
    }

    if(state.fromWhere == "slowOff") {
        slowDimmerDn.each {
            if (it.hasCommand('setColor')) {
                if(logEnable && logSize) log.debug "In setLevelandColorHandler - $it.displayName, setColor: $value"
                it.setColor(value)
            } else if (it.hasCommand('setLevel')) {
                if(logEnable && logSize) log.debug "In setLevelandColorHandler - $it.displayName, setLevel: $value"
                it.setLevel(level as Integer ?: 99)
            } else {
                if(logEnable && logSize) log.debug "In setLevelandColorHandler - $it.displayName, on()"
                it.on()
            }
        }
    }
    
    if(state.fromWhere == "permanentDimHandler") {
        setOnLC.each {
            if(pdColor && it.hasCommand('setColor')) {
                def value = [hue: hueColor, saturation: saturation, level: onLevel]
                if(logEnable && logSize) log.debug "In setLevelandColorHandler - PD - $it.displayName, setColor: $value"
                it.setColor(value)
            } else if(pdTemp && it.hasCommand('setColorTemperature')) {
                if(logEnable && logSize) log.debug "In setLevelandColorHandler - PD - $it.displayName, setColorTemp: $pdTemp, level: ${permanentDimLvl} (or warningLvl: ${warningDimLvl})"
                pauseExecution(actionDelay)
                if(permanentDimLvl) { it.setLevel(permanentDimLvl) }
                if(warningDimLvl) { it.setLevel(warningDimLvl) }
                pauseExecution(actionDelay)
                it.setColorTemperature(pdTemp)
            } else {
                if(logEnable && logSize) log.debug "In setLevelandColorHandler - PD - $it.displayName, setLevel: $permanentDimLvl (or warningLvl: ${warningDimLvl})"
                pauseExecution(actionDelay)
                if(permanentDimLvl) { it.setLevel(permanentDimLvl) }
                if(warningDimLvl) { it.setLevel(warningDimLvl) }
            }
        }
    }
    
    if(state.fromWhere == "permanentDimPerHandler") {
        theDevice = state.dimmerDevices
        if(logEnable) log.debug "In setLevelandColorHandler - switchesPerMode - Working on: ${theDevice}"
        if(theDevice.hasCommand('setColor') && state.onTemp == "NA") {
            def value = [hue: hueColor, saturation: saturation, level: onLevel]
            if(logEnable && logSize) log.debug "In setLevelandColorHandler - switchesPerMode - $it.displayName, setColor: $value"
            pauseExecution(actionDelay)
            theDevice.setColor(value)
        } else if(theDevice.hasCommand('setColorTemperature') && state.onColor == "NA") { 
            if(logEnable && logSize) log.debug "In setLevelandColorHandler - switchesPerMode - $it.displayName, setColorTemp: $pdTemp, level: ${permanentDimLvl} (or warningLvl: ${warningDimLvl})"
            pauseExecution(actionDelay)
            if(permanentDimLvl) { theDevice.setLevel(permanentDimLvl) }
            if(warningDimLvl) { theDevice.setLevel(warningDimLvl) }
            pauseExecution(actionDelay)
            theDevice.setColorTemperature(pdTemp)
        } else {
            if(logEnable && logSize) log.debug "In setLevelandColorHandler - switchesPerMode - $it.displayName, setLevel: $permanentDimLvl (or warningLvl: ${warningDimLvl})"
            pauseExecution(actionDelay)
            if(permanentDimLvl) { theDevice.setLevel(permanentDimLvl) }
            if(warningDimLvl) { theDevice.setLevel(warningDimLvl) }
        }
    }
}

def getLockCodeNames(myDev) {  // Special thanks to Bruce @bravenel for this code
    def list = []
    myDev.each {
        //log.warn "Working on Lock: ${it}"
        list += getLockCodesFromDevice(it).tokenize(",")
    }
    lista = list.flatten().unique{ it }
    listb = lista.sort { a, b -> a <=> b }
    return listb
}

def getLockCodesFromDevice(device) {  // Special thanks to Bruce @bravenel for this code
    def lcText = device?.currentValue("lockCodes")
    if (!lcText?.startsWith("{")) {
        lcText = decrypt(lcText)
    } 
    def lockCodes
    if(lcText) lockCodes = new JsonSlurper().parseText(lcText)
    def result = ""
    lockCodes.each {if(it.value.name) result += it.value.name + ","}
    return result ? result[0..-2] : ""
}

def sendSettingsToParentHandler() {
    if(logEnable) log.debug "In sendSettingsToParentHandler (${state.version})"
    parent.getSettingsFromChild(mySettings)
}

def globalVariablesHandler(data) {
    if(data) { state.gvMap = data }
    if(globalVariableEvent) startTheProcess()
}

def versionIsGoodHandler(data) {
    if(data) { state.verIsGood = data }
}

def cronExpressionsHandler(data) {
    if(data) { state.ceMap = data }
}

def sdPerModeHandler(data) {
    if(logEnable) log.debug "In sdPerModeHandler (${state.version}) - data: ${data}"
    def (theType, newData) = data.split(";")
    if(state.sdPerModeMap == null) state.sdPerModeMap = [:]
    theName = sdPerModeName.toString()
    if(theType == "add") {
        if(sdPerModeLevel == null) sdPerModeLevel = "NA"
        if(sdPerModeTemp == null) sdPerModeTemp = "NA"
        if(sdPerModeColor == null) sdPerModeColor = "NA"        
        theValue = "${setDimmersPerMode}:${sdPerModeLevel}:${sdPerModeTemp}:${sdPerModeColor}"
        state.sdPerModeMap.put(theName,theValue)
    } else if(theType == "del") {
        state.sdPerModeMap.remove(theName)
    }      
    if(state.sdPerModeMap) {
        thePerModeMap =  "<table width=90% align=center><tr><td><b><u>Mode</u></b><td><b><u>Devices</u></b><td><b><u>Level</u></b><td><b><u>Temp</u></b><td><b><u>Color</u></b>"
        def theData = "${state.sdPerModeMap}".split(",")
        theData.each { it -> 
            def (name, theDevices, theLevel, theTemp, theColor) = it.split(":")
            if(name.startsWith(" ") || name.startsWith("[")) name = name.substring(1)
            theColor = theColor.replace("]","")
            thePerModeMap += "<tr><td>${name}<td>${theDevices}<td>${theLevel}<td>${theTemp}<td>${theColor}"
        }                
        thePerModeMap += "</table>"
    }
    state.thePerModeMap = thePerModeMap    
    app.removeSetting("setDimmersPerMode")
    app.removeSetting("sdPerModeName")
    app.removeSetting("sdPerModeLevel")
    app.removeSetting("sdPerModeTemp")
    app.removeSetting("sdPerModeColor")
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    if(sdPerModeName && state.whichButton == "sdPerModeDel"){
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        sdPerModeHandler("del;nothing")
    } else if(sdPerModeName && state.whichButton == "sdPerModeAdd"){
        if(logEnable) log.debug "In appButtonHandler - Working on: ${state.whichButton}"
        sdPerModeHandler("add;nothing")
    } else if(sdPerModeName && state.whichButton == "sdPerModeRefresh"){
        sdPerModeHandler("refresh;nothing")
    } else if(sdPerModeName && state.whichButton == "sdPerModeClear"){
        state.sdPerModeMap = [:]
        state.thePerModeMap = null
    } else if(state.whichButton == "resetMaps") {
        if(state.setOldMap == null) state.setOldMap = false
        if(state.setOldMapPer == null) state.setOldMapPer = false
        if(state.setOldLevelMap == null) state.setOldLevelMap = false
        if(state.setOldLevelMapPer == null) state.setOldLevelMapPer = false
    }
}

// ********** Normal Stuff **********
def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app.updateSetting("logEnable",[value:"false",type:"bool"])
}

def checkEnableHandler() {
    state.eSwitch = false
    if(disableSwitch) { 
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            theStatus = it.currentValue("switch")
            if(theStatus == "on") { state.eSwitch = true }
        }
        if(logEnable) log.debug "In checkEnableHandler - eSwitch: ${state.eSwitch}"
    }
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText="") {			// Modified from @Stephack Code
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display(data) {
    if(data == null) data = ""
    setVersion()
    getHeaderAndFooter()
    if(app.label) {
        if(app.label.contains("(Paused)")) {
            theName = app.label - " <span style='color:red'>(Paused)</span>"
        } else {
            theName = app.label
        }
    }
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {
        paragraph "${state.headerMessage}"
        paragraph getFormat("line")
        input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true
    }
}

def display2() {
    section() {
        paragraph getFormat("line")
        paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>"
        paragraph "${state.footerMessage}"
    }
}

def getHeaderAndFooter() {
    timeSinceNewHeaders()
    if(state.totalHours > 4) {
        def params = [
            uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json",
            requestContentType: "application/json",
            contentType: "application/json",
            timeout: 30
        ]
        try {
            def result = null
            httpGet(params) { resp ->
                state.headerMessage = resp.data.headerMessage
                state.footerMessage = resp.data.footerMessage
            }
        }
        catch (e) { }
    }
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>"
}

def timeSinceNewHeaders() { 
    if(state.previous == null) { 
        prev = new Date()
    } else {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000"))
    }
    def now = new Date()
    use(TimeCategory) {
        state.dur = now - prev
        state.days = state.dur.days
        state.hours = state.dur.hours
        state.totalHours = (state.days * 24) + state.hours
    }
    state.previous = now
}
