if (parseFloat(db.version()) < 3.4) throw new Error("'Create View' requires MongoDB 3.4 or above")
db = db.getSiblingDB('StarWars')
db.people.aggregate([
    {
        $lookup: {
            from: "planets",
            localField: "homeworld",
            foreignField: "url",
            as: "homeworld",
            pipeline: [{$project: {name: 1, _id: 0}}]
        }
    },
    {
        $lookup: {
            from: "species",
            localField: "species",
            foreignField: "url",
            as: "species",
            pipeline: [{$project: {name: 1, _id: 0}}]
        }
    },
    {
        $lookup: {
            from: "films",
            localField: "films",
            foreignField: "url",
            as: "films",
            pipeline: [{$project: {title: 1, _id: 0}}]
        }
    },
    {
        $project: {
            starships: 0,
            url: 0,
            vehicles: 0,
            created: 0,
            edited: 0,
            _id: 0
        }
    },

]).saveAsView("peopleResolved",{dropIfExists:true})
