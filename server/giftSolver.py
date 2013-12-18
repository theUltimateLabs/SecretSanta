from constraint import *
import random


def GetGiftSolution(family,maxDelay=2.0):
    #Two small to get a solution
    if len(family) < 2:
        return None
    
    problem = Problem()
    #add each variable and domain to the problem
    #reduce the domain as much as possible on this first pass
    for member in family:
        problem.addVariable(f,list(family - set(member)))
    problem.addConstraint(AllDifferentConstraint()) #Can't have two people giving to the same person

    
    #for some cases it will take a very long time to generate all the solutions,
    #so this generates as many as possible in a given amount of time and then
    #randomly chooses one solution from the list
    solutions = []
    solutionIter = problesm.getSolutionIter()
    start = time.time()
    while(time.time()-start < maxDelay):
        try:
            solutions.append(solutionIter.next())
        except StopIteration:
            break
    return random.choice(solutions)

class Tester(unittest.TestCase):

    def setUp(self):
        self.scenarios = map(set,["","A","AB","ABC","ABCD","ABCDEFGHIJKLMNOPQRSTUVWXYZ"])

    def checkSolution(self,scenario,solution):
        if len(family) < 2:
            self.assertIsNone(solution)
            return
        
        self.assertEqual(len(scenario),len(solution))
        giftees = set()
        for gifter in scenario:
            self.assertIn(gifter,solution)
            giftee = solution[gifter]
            self.assertIn(giftee,scenario)
            self.assertNotEqual(gifter,giftee)
            giftees.add(giftee)
        self.assertEqual(len(giftees),len(scenario))
        
        
    def problem1(self):
        for scenario in self.scenarios:
            solution = getSolution(scenarios)
            checkSolution(scenario,solution)
                

if __name__ == '__main__':
    unittest.main()
    
    