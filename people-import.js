const axios=require("axios");

db = db.getSiblingDB('StarWars')
let people = db.getCollection('people')
people.deleteMany({})

let hasNext = true;
let url = 'https://swapi.dev/api/people'
while(hasNext){
    console.log(`Loading people from ${url} ...`)
    let rst=await (axios.get(url));
    if (rst.data.next) {
        hasNext = true;
        url = rst.data.next;
    } else {
        hasNext = false;
    }
    let peopleData = rst.data.results;
    people.insertMany(peopleData)
    console.log(`Inserted ${peopleData.length} people...`)
}

console.log(`Finished with ${people.estimatedDocumentCount()} people.`)