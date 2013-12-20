from constraint import *
import random,unittest,time

#Custom constraint that prevents cycles
#i.e. a gifter cannot be the giftee of the same person
class NoCycles(Constraint):
    
    def __init__(self):
        pass
    
    def __call__(self, variables, domains, assignments, forwardcheck=False):
        for x,y in assignments.items():
            if y in assignments and x == assignments[y]:
                return False
        return True


# Uses a constraint satisfaction solver to find a secret santa solution
# maxDelay will limit how much time is spent finding possible solutions
# historyRange is how many years to take into account from the history array
# restrcitWithinFmaily restricts gifting to only people outside a participants immediate family
# returns a single solution or None if no solution is found
def findGiftSolution(families,history=[],maxDelay=1.0,historyRange=0,restrictWithinFamily=False,noCycles=False):
    #Two small to get a solution
    
    problem = Problem()
    extFamily = set()
    for family in families:
        extFamily |= family
    if len(extFamily) < 2:
        print 'Must be at least two members'
        return None
    
    #add each variable and domain to the problem
    #reduce the initial domain as much as possible on this first pass     
    for family in families:
        for gifter in family:
            #search through history and extract all history of a gifter
            gifterHistory = set()
            for pastSolution in history:
                if historyRange <= 0: break
                historyRange -= 1
                if gifter in pastSolution:
                    gifterHistory.add(pastSolution[gifter])
            
            domain = extFamily - set(gifter) - gifterHistory
            if restrictWithinFamily:
                #print 'gifter',gifter,'family',family,'domain',domain
                domain -= family
            if len(domain) == 0:
                print 'No solution'
                return None
            problem.addVariable(gifter,list(domain))
            
    problem.addConstraint(AllDifferentConstraint()) #Can't have two people giving to the same person
    if noCycles:
        problem.addConstraint(NoCycles())
    
    #for some cases it will take a very long time to generate all the solutions,
    #so this generates as many as possible in a given amount of time and then
    #randomly chooses one solution from the list
    solutions = []
    solutionIter = problem.getSolutionIter()
    start = time.time()
    while(time.time()-start < maxDelay):
        try:
            solutions.append(solutionIter.next())
        except StopIteration:
            break
    if len(solutions) == 0:
        print 'No Solution'
        return None
    return random.choice(solutions)

class Tester(unittest.TestCase):

    #check if a solution is valid
    def checkSolution(self,families,solution,history=[],historyRange=0,restrictWithinFamily=False):
        if (solution == None):
            return
        
        extFamily = set()
        for family in families:
            extFamily |= family
        
        if len(extFamily) < 2:
            self.assertIsNone(solution)
            return
        
        self.assertEqual(len(extFamily),len(solution))
        giftees = set()
        for family in families:
            for gifter in family:
                self.assertIn(gifter,solution) #make sure each gifter is in solution
                giftee = solution[gifter] 
                #make sure no repeats with history range
                for pastSolution in history:
                    if historyRange <= 0: break
                    historyRange -= 1
                    if gifter in pastSolution:
                        self.assertNotEqual(giftee, pastSolution[gifter])
                self.assertIn(giftee,extFamily)
                self.assertNotEqual(gifter,giftee) #not giving to self
                if restrictWithinFamily:
                    self.assertNotIn(giftee,family) # not giving to smoneone in same family
                giftees.add(giftee)
        self.assertEqual(len(giftees),len(extFamily)) #number of figters equals number of giftees
        
    
    def test_problem3(self):
        print 'Testing problem3'
        familiess = []
        
        familiess.append(map(set,["A"]))
        familiess.append(map(set,["A","BC"]))
        familiess.append(map(set,["A","BC","DEF"]))
        familiess.append(map(set,["A","BC","DEF","GHIJ"]))
        familiess.append(map(set,["A","BC","DEF","GHIJ","KLMNO"]))
         
        for families in familiess:
            history = []
            for year in range(4):
                solution = findGiftSolution(families,history,historyRange=3,restrictWithinFamily=True)
                if not solution: break
                print solution
                self.checkSolution(families,solution,history,historyRange=3,restrictWithinFamily=True)
                history.append(solution)
                
                
    def test_problem2(self):
        print 'Testing problem2'
        families = map(set,["","A","AB","ABC","ABCD","ABCDEFGHIJKLMNOPQRSTUVWXYZ"])
        for family in families:
            history = []
            for year in range(4):
                solution = findGiftSolution([family],history,historyRange=3)
                if not solution: break
                print solution
                self.checkSolution([family],solution,history,historyRange=3)
                history.append(solution)
                
    def test_problem1(self):
        print 'Testing problem1'
        families = map(set,["","A","AB","ABC","ABCD","ABCDEFGHIJKLMNOPQRSTUVWXYZ"])
        for family in families:
            solution = findGiftSolution([family])
            print solution
            self.checkSolution([family],solution)
                

if __name__ == '__main__':
    unittest.main()
    
    