// Playground - noun: a place where people can play

import UIKit

var testMaps: [String: String] = [:]


var airports: [String: String] = ["YYZ": "Tornoto Pearson", "DUB":"Dublin"]
airports["DUB"] = "test"
//airports.updateValue("Dublin Airport", forKey: "DUB")

for(key,value) in airports
{
    println("\(key): \(value)")
}


var threeDoubles = [Double](count: 3, repeatedValue: 0.0)
var anotherThreeDoubles = [Double](count: 3, repeatedValue: 3.5)
var sixDoubles = threeDoubles + anotherThreeDoubles


var emptyArray: [Int] = []
emptyArray.append(2)

for(index,value) in enumerate(emptyArray)
{
    println("Item \(index+1):\(value)")
}



var arrayTest: [String] = ["Egg", "Milk"]
var stringArr = ["item0", "item1"]
stringArr += ["item2", "item3"]
stringArr.append("item4")
stringArr[0...2] = ["item00"]
stringArr.insert("item0", atIndex: 1)
stringArr.removeAtIndex(0)
stringArr.removeLast()
for item in stringArr
{
    println(item)
}

for (index,value) in enumerate(stringArr)
{
    println("Item \(index+1):\(value)")
}





var str = "Hello, playground"
str += " welcome!"

let yenSign: Character = "Y"
countElements(str)
str.utf16Count


var x: Int = 14
var y = 20
var z: Double = 20.1
let strWithValue = "we have \(x+y) apples"
let strWithIntAndFloat = "test int \(x),\(z)"
var shoppingList = ["catfish", "water", "tulips", "blue paint"]
shoppingList[1] = "bottle of water"
var occupations = [
    "Malcolm": "Captain",
    "Kaylee": "Mechanic",
]
occupations["Jayne"] = "Public Relations"

for(key,value) in occupations
{
    //if nil != key
    println(" \(key): \(value)")
}

for item in shoppingList
{
    if !item.isEmpty
    {
        println(item)
    }
}

var optionalName: String? = "John AppleSeed"
var greeting = "Hello!"
if let name = optionalName
{
    greeting = "Hello, \(name)"
}

optionalName = nil
if let name = optionalName
{
    greeting = "Hello, no name"
}
else
{
    greeting = "Hello, \(optionalName)"
}

//switch
let vegetable = "red pepper"
switch(vegetable)
{
    case "celery":
        let vegetableComment = "Add some rs"
    default:
        let vegetableComment = "Everything"
}

for i in 0...4
{
    println("\(i)")
}

func greet(name: String, day: String)
{
    println("Hello \(name), today is \(day).")
}
greet("Bob","Tuesday")

func calculateStatistics(scores:[Int]) -> (min: Int, max: Int, sum: Int)
{
    var min = scores[0]
    var max = scores[0]
    var sum = 0
    
    for score in scores {
        if score > max {
            max = score
        } else if score < min {
            min = score
        }
        sum += score
    }
    
    return (min, max, sum)
}
let statistics = calculateStatistics([1,5,6,9])
statistics.min
statistics.max
statistics.sum
statistics.0

func returnFifteen() -> Int
{
    var y = 10
    func add()
    {
         y += 5
    }
    add()
    return y
}
returnFifteen()

func makeIncrement()-> (Int->Int)
{
    func addOne(number: Int) -> Int
    {
        return 1 + number
    }
    return addOne
}
var increment = makeIncrement()
increment(7)

func hasAnyMatches(list:[Int], condition:Int->Bool)->Bool
{
    for item in list
    {
        if condition(item)
        {
            return true
        }
    }
    return false
}

func lessThanTen(number: Int)->Bool
{
    return number < 10
}
var numbers = [11,50,70,20]
hasAnyMatches(numbers, lessThanTen)

class NamedShape
{
    var numberOfSides: Int = 0
    var name: String
   
    init(name:String)
    {
        self.name = name
    }
    deinit
    {
        
    }
    
    func simpleDescription()->String
    {
        return "A Shape with \(numberOfSides) sides."
    }
}

var shape = NamedShape(name:"Rectangle")
shape.numberOfSides = 4
shape.simpleDescription()

class Square: NamedShape
{
    var sideLength:Double
    
    init(sideLength:Double,name:String)
    {
        self.sideLength = sideLength
        super.init(name: name)
        numberOfSides = 4
    }
    
    func area()->Double
    {
        return sideLength*sideLength
    }
    
    override func simpleDescription() -> String {
        return "A Square with sides of lengh \(sideLength)."
    }
}

let square = Square(sideLength: 1.5, name: "my test square")
square.area()
square.simpleDescription()

enum Rank: Int
{
    case Ace = 1
    case Two, Three, Four, Five
    case Jack, Queen, King
    func simpleDescription()->String
    {
        switch self
        {
        case .Ace:
                return "ace"
        case .Jack:
                return "jack"
            default:
                return String(self.rawValue)
        }
    }
}

let ace = Rank.Ace
ace.rawValue
Rank.Jack.rawValue

enum Suit
{
    case Spades,Hearts
    
}
let hearts = Suit.Hearts

protocol ExampleProtocol
{
    var simpleDescription: String{ get }
    mutating func adjust()
}

class SimpleClass: ExampleProtocol
{
    var simpleDescription: String = "A very simple class."
    var anotherProperty:Int = 655;
    func adjust() {
        simpleDescription += " Now 100% adjusted."
    }
}

var a = SimpleClass()
a.adjust()

extension Int: ExampleProtocol
{
    var simpleDescription :String
    {
        return "The number \(self)"
    }
    
    func adjust()
    {
    }
}
7.simpleDescription























