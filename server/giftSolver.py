from constraint import *
import random,unittest,time


def GetGiftSolution(families,history=[],maxDelay=1.0,historyYears=0,restrictWithinFmaily=False):
    #Two small to get a solution
    
    problem = Problem()
    extFamilySet = set()
    for family in families:
        extFamilySet |= family
    if len(extFamilySet) < 2:
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
            
            domain = extFamilySet - set(gifter) - gifterHistory
            if restrictWithinFmaily:
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


    def checkSolution(self,scenario,solution,history=[]):
        if (solution == None):
            return
        
        if len(scenario) < 2:
            self.assertIsNone(solution)
            return
        
        self.assertEqual(len(scenario),len(solution))
        giftees = set()
        for gifter in scenario:
            self.assertIn(gifter,solution)
            giftee = solution[gifter]
            for pastSolution in history:
                if gifter in pastSolution:
                    self.assertNotEqual(giftee, pastSolution[gifter])
            self.assertIn(giftee,scenario)
            self.assertNotEqual(gifter,giftee)
            giftees.add(giftee)
        self.assertEqual(len(giftees),len(scenario))
        
    
    def test_problem3(self):
        print 'Testing problem3'
        scenarios = []
        
        scenarios.append(map(set,["A"]))
        scenarios.append(map(set,["A","BC"]))
        scenarios.append(map(set,["A","BC","DEF"]))
        scenarios.append(map(set,["A","BC","DEF","GHIJ"]))
        scenarios.append(map(set,["A","BC","DEF","GHIJ","KLMNO"]))
         
        for scenario in scenarios:
            history = []
            for y in range(4):
                solution = GetGiftSolution(scenario,history,historyYears=3,restrictWithinFmaily=True)
                if not solution: break
                print solution
                #self.checkSolution(scenario,solution,history)
                history.append(solution)
                
                
    def test_problem2(self):
        print 'Testing problem2'
        scenarios = map(set,["","A","AB","ABC","ABCD","ABCDEFGHIJKLMNOPQRSTUVWXYZ"])
        for scenario in scenarios:
            history = []
            for y in range(4):
                solution = GetGiftSolution([scenario],history,historyYears=3)
                if not solution: break
                print solution
                #self.checkSolution(scenario,solution,history)
                history.append(solution)
                
    def test_problem1(self):
        print 'Testing problem1'
        scenarios = map(set,["","A","AB","ABC","ABCD","ABCDEFGHIJKLMNOPQRSTUVWXYZ"])
        for scenario in scenarios:
            solution = GetGiftSolution([scenario])
            print solution
            #self.checkSolution(scenario,solution)
                

if __name__ == '__main__':
    unittest.main()
    
    