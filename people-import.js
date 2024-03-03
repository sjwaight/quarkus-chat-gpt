const axios=require("axios");

db = db.getSiblingDB('StarWars')

for (let resource of ["planets", "starships", "vehicles", "people", "films", "species"]) {
    let resourceCollection = db.getCollection(resource)
    resourceCollection.deleteMany({})

    let hasNext = true;
    let url = `https://swapi.dev/api/${resource}`
    while(hasNext){
        console.log(`Loading ${resource} from ${url} ...`)
        let rst=await (axios.get(url));
        if (rst.data.next) {
            hasNext = true;
            url = rst.data.next;
        } else {
            hasNext = false;
        }
        let data = rst.data.results;
        resourceCollection.insertMany(data)
        console.log(`Inserted ${data.length} ${resource}...`)
    }

    console.log(`Finished with ${resourceCollection.estimatedDocumentCount()} ${resource}.`)
}