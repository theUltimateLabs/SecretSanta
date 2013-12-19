from constraint import *
import random,unittest,time


def GetGiftSolution(families,history=[],maxDelay=1.0,historyYears=0,restrictWithinFamily=False):
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
                if historyYears <= 0: break
                historyYears -= 1
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


    def checkSolution(self,families,solution,history=[],historyYears=0,restrictWithinFamily=False):
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
                    if historyYears <= 0: break
                    historyYears -= 1
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
                solution = GetGiftSolution(families,history,historyYears=3,restrictWithinFamily=True)
                if not solution: break
                print solution
                self.checkSolution(families,solution,history,historyYears=3,restrictWithinFamily=True)
                history.append(solution)
                
                
    def test_problem2(self):
        print 'Testing problem2'
        families = map(set,["","A","AB","ABC","ABCD","ABCDEFGHIJKLMNOPQRSTUVWXYZ"])
        for family in families:
            history = []
            for year in range(4):
                solution = GetGiftSolution([family],history,historyYears=3)
                if not solution: break
                print solution
                self.checkSolution([family],solution,history,historyYears=3)
                history.append(solution)
                
    def test_problem1(self):
        print 'Testing problem1'
        families = map(set,["","A","AB","ABC","ABCD","ABCDEFGHIJKLMNOPQRSTUVWXYZ"])
        for family in families:
            solution = GetGiftSolution([family])
            print solution
            self.checkSolution([family],solution)
                

if __name__ == '__main__':
    unittest.main()
    
    