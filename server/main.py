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
import time,random,json

class MainHandler(webapp2.RequestHandler):
    def get(self,path):

        if path == 'login':
            listname = self.request.get('listname')
            password = self.request.get('password')
            if not listname or not password:
                self.error(400)
                return
            self.response.headers['Content-Type'] = 'application/json'
            obj = {
                'success': 'some var',
                'payload': 'some var',
            }
            self.response.out.write(json.dumps(obj))
        else:
            self.error(400) #Bad Request


app = webapp2.WSGIApplication([
    ('/(.*)', MainHandler)
], debug=True)


class NoCyles(Constraint):

    def __init__(self):
        pass

    def __call__(self, variables, domains, assignments, forwardcheck=False):
        for x,y in assignments.items():
            if y in assignments and x == assignments[y]:
                return False
        return True

if __name__ == "__main__":
    family1 = set("AB")
    family2 = set("DE")
    family3 = set("GH")
    extFamily = family1|family2|family3
    problem = Problem()
    for fam in (family1,family2,family3):
        for f in fam:
            problem.addVariable(f,list(extFamily-fam))
    problem.addConstraint(NoCyles())
    problem.addConstraint(AllDifferentConstraint())

    solutions = []
    solutionIter = problem.getSolutionIter()
    start = time.time()
    while(time.time()-start < 2):
        try:
            solutions.append(solutionIter.next())
        except StopIteration:
            break
    solutions.sort()
    print len(solutions),solutions[0],solutions[-2]
    print random.choice(solutions)