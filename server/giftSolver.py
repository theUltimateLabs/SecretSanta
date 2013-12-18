from constraint import *
import random,unittest,time


def GetGiftSolution(family,history=[],maxDelay=2.0):
    #Two small to get a solution
    if len(family) < 2:
        print 'Must be at least two members'
        return None
    
    problem = Problem()
    #add each variable and domain to the problem
    #reduce the domain as much as possible on this first pass
    for gifter in family:
        #search through history and extract all history of a gifter
        gifterHistory = set()
        for pastSolution in history:
            if gifter in pastSolution:
                gifterHistory.add(pastSolution[gifter])
        domain = family - set(gifter) - gifterHistory
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
        
    
    def test_problem2(self):
        print 'Testing problem2'
        self.scenarios = map(set,["","A","AB","ABC","ABCD","ABCDEFGHIJKLMNOPQRSTUVWXYZ"])
        self.scenarios = map(set,["","A","AB","ABC","ABCD","ABCDEFGHIJKLMNOPQRSTUVWXYZ"])
        for scenario in self.scenarios:
            history = []
            for y in range(3):
                solution = GetGiftSolution(scenario,history)
                if not solution: break
                print solution
                self.checkSolution(scenario,solution,history)
                history.append(solution)
                
    def test_problem1(self):
        print 'Testing problem1'
        self.scenarios = map(set,["","A","AB","ABC","ABCD","ABCDEFGHIJKLMNOPQRSTUVWXYZ"])
        for scenario in self.scenarios:
            solution = GetGiftSolution(scenario)
            print solution
            self.checkSolution(scenario,solution)
                

if __name__ == '__main__':
    unittest.main()
    
    