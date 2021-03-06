#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
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
import webapp2
from constraint import *
import time,random,json,binascii
import logging,hashlib,os
from giftSolver import findGiftSolution
from google.appengine.api import mail
from google.appengine.api import users
from google.appengine.ext import db
from datetime import date


#marshals data from json to familes array
#also creates email array with the default email of each participent
def decodeJson(jsonStr):
    listJson = json.loads(jsonStr)
    families = []
    emails = {}
    extFamily = []
    for familyJson in listJson:
        family = set()
        family.add(familyJson['name'])
        emails[familyJson['name']] = familyJson['email']
        extFamily.append(familyJson['name'])
        for memberJson in familyJson['members']:
            if 'email' in memberJson:
                emails[memberJson['name']] = memberJson['email']
            else:
                emails[memberJson['name']] = familyJson['email']
            family.add(memberJson['name'])
            extFamily.append(memberJson['name'])
        families.append(family)
    return families,emails,extFamily

def sendEmail(gifter,gifterEmail,giftee,gifteeEmail):
    logging.info("Sending an email to %s from %s" % (gifterEmail,gifteeEmail))
    message = mail.EmailMessage()
    message.sender = "elf@theultimatesecretsanta.appspotmail.com"
    message.to = gifterEmail
    message.subject = "Secret Santa Assignment for %s!" % (gifter)
    message.body = """
    
    
    **************************************************************************
         You have been assigned to give to %s this year.
    **************************************************************************
    
    
    
    This list list generated by the Top Secret Santa App from theUltimateLabs.com""" % (giftee)
    message.send()


class SolutionEntry(db.Model):
  listname = db.StringProperty(required=True)
  year = db.IntegerProperty(required=True)
  gifter = db.StringProperty(required=True)
  giftee = db.StringProperty(required=True)

class ListnameEntry(db.Model):
  listname = db.StringProperty(required=True)
  password = db.StringProperty(required=True)
  salt = db.StringProperty(required=True)

class MainHandler(webapp2.RequestHandler):
    
    def verifyPassword(self):
        listname = self.request.get('listname')
        password = self.request.get('password')
        if not listname or not password:
            self.error(400)
            return False
        q = ListnameEntry.all()
        q.filter("listname =", listname)
        listnameEntry = q.get()
        if listnameEntry:
            salt = listnameEntry.salt
            calcPassword = hashlib.sha1(password + salt).hexdigest()
            if calcPassword == listnameEntry.password:
                return True
        
        self.error(401)             
        return False
        
    def createListname(self):
        listname = self.request.get('listname')
        password = self.request.get('password')
        logging.info('listname:'+listname+" password:"+password)
        if not listname or not password:
            self.error(400)
            return False
        q = ListnameEntry.all()
        q.filter("listname =", listname)
        listnameEntry = q.get()
        logging.info(listnameEntry)
        if listnameEntry:
            self.error(401) 
            return False
    
        salt = binascii.hexlify(os.urandom(160))
        password = hashlib.sha1(password + salt).hexdigest()
        listnameEntry = ListnameEntry(key_name=listname,listname=listname,salt=salt,password=password)
        logging.info('salt:'+listnameEntry.salt+" password:"+listnameEntry.password)
        listnameEntry.put()
        return True
    
                    
        
    def get(self,path):
        listname = self.request.get('listname')
        if path == 'login':
            self.verifyPassword();
            
            #return history
            self.response.headers['Content-Type'] = 'application/json'
            currentYear = date.today().year
            histories = {}
            for yearOffset in range(5):
                year = currentYear - yearOffset
                pastSolution = {}    
                q = SolutionEntry.all()
                q.filter("listname =", listname)
                q.filter("year =", year)
                for entry in q.run():
                    pastSolution[entry.gifter] = entry.giftee
                histories[str(year)] = pastSolution
                
            self.response.out.write(json.dumps(histories))
        elif path == 'create':
            self.createListname()
        else:
            self.error(400) #Bad Request
            
    def put(self,path):
        self.verifyPassword()
        listname = self.request.get('listname')
        if path == 'submit':
            year = date.today().year
            restrictWithinFamily = self.request.get('restrictWithinFamily').lower() == 'true'
            noCycles = self.request.get('noCycles').lower() == 'true'
            historyRange = int(self.request.get('historyRange'))
            
            print listname,restrictWithinFamily,noCycles,historyRange
            #marshal data from json to families array of sets
            families,emails,extFmaily = decodeJson(self.request.body)
            
            logging.info(families)
            logging.info(emails)
            histories = []
            for y in range(1,historyRange+1):
                pastSolution = {}
                for gifter in extFmaily:
                    q = SolutionEntry.all()
                    q.filter("listname =", listname)
                    q.filter("year =", year)
                    q.filter("gifter =", gifter)
                    pastEntry = q.get()
                    if pastEntry:
                        pastSolution[gifter] = pastEntry.giftee
                histories.append(pastSolution)
                    
                    
            solution = findGiftSolution(families,histories,
                        historyRange=historyRange,
                        restrictWithinFamily=restrictWithinFamily,
                        noCycles=noCycles)
            
            if not solution:
                self.error(500)
                return
            
            logging.info("Sending back solution")
            logging.info(json.dumps(solution))
            self.response.out.write(json.dumps(solution))
            
            for gifter,giftee in solution.items():
                SolutionEntry(key_name=listname+str(year)+gifter,year=year,gifter=gifter,giftee=giftee,listname=listname).put()
                sendEmail(gifter,emails[gifter],giftee,emails[giftee])
            
            
            
            
        elif path == 'history':
            year = int(self.request.get('year'))
            logging.info(json.loads(self.request.body)) 
            for gifter,giftee in json.loads(self.request.body).items():
                SolutionEntry(key_name=listname+str(year)+gifter,year=year,gifter=gifter,giftee=giftee,listname=listname).put()
            
        else:
            self.error(400) #Bad Request



app = webapp2.WSGIApplication([
    ('/(.*)', MainHandler)
], debug=True)


