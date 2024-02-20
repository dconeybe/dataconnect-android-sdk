package graphql

import (
	"github.com/vektah/gqlparser/v2"
	"github.com/vektah/gqlparser/v2/ast"
	"github.com/vektah/gqlparser/v2/gqlerror"
	"github.com/vektah/gqlparser/v2/parser"
	"log"
	"os"
)

func LoadSchemaFile(file string) (*ast.Schema, error) {
	sources, err := loadPreludeSources()
	if err != nil {
		return nil, err
	}

	log.Println("Loading GraphQL schema file:", file)
	source, err := loadGraphQLSourceFromFile(file)
	if err != nil {
		return nil, err
	}
	sources = append(sources, source)

	graphqlSchema, err := gqlparser.LoadSchema(sources...)
	if err != nil {
		return nil, err
	}

	addUnderscoreDataInputTypes(graphqlSchema)

	return graphqlSchema, nil
}

func addUnderscoreDataInputTypes(graphqlSchema *ast.Schema) {
	dataTypeDefinitions := make([]*ast.Definition, 0, 0)
	for _, typeInfo := range graphqlSchema.Types {
		if typeInfo.BuiltIn {
			continue
		}

		dataTypeDefinition := new(ast.Definition)
		*dataTypeDefinition = *typeInfo
		dataTypeDefinition.Name = typeInfo.Name + "_Data"
		dataTypeDefinition.Kind = ast.InputObject
		dataTypeDefinitions = append(dataTypeDefinitions, dataTypeDefinition)
	}

	for _, dataTypeDefinition := range dataTypeDefinitions {
		log.Println("Adding input type to schema: ", dataTypeDefinition.Name)
		graphqlSchema.Types[dataTypeDefinition.Name] = dataTypeDefinition
	}
}

func LoadOperationsFile(file string, schema *ast.Schema) (*ast.QueryDocument, error) {
	log.Println("Loading GraphQL operations file:", file)
	source, err := loadGraphQLSourceFromFile(file)
	if err != nil {
		return nil, err
	}

	query, err := parser.ParseQuery(source)
	if err != nil {
		gqlErr, ok := err.(*gqlerror.Error)
		if ok {
			return nil, gqlerror.List{gqlErr}
		}
		return nil, gqlerror.List{gqlerror.Wrap(err)}
	}

	return query, nil
}

func loadGraphQLSourceFromFile(file string) (*ast.Source, error) {
	fileBytes, err := os.ReadFile(file)
	if err != nil {
		return nil, err
	}
	return &ast.Source{Name: file, Input: string(fileBytes), BuiltIn: false}, nil
}